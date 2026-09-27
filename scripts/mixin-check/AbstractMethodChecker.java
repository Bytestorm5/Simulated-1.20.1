import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Finds concrete classes that leave an interface method unimplemented, resolving everything from the classpath.
 * <p>
 * Run against reobfuscated jars with SRG-named Minecraft on the classpath, this catches interfaces that declare a
 * vanilla method name (e.g. {@code Level getLevel()} for a block entity): in dev the vanilla method implements it, but
 * after reobfuscation the vanilla method is {@code m_58898_} and the class throws {@link AbstractMethodError}.
 * <p>
 * It also resolves every method call in those jars. The reobfuscator can rename a call through an interface to the SRG
 * name of a colliding vanilla method while the interface keeps the dev name, which throws {@link NoSuchMethodError}.
 * <p>
 * <p>
 * With {@code --mappings <namedToIntermediate.tsrg>} it also checks mixin classes for methods meant to override a
 * Minecraft method that kept their dev name. A mixin method without a mixin annotation that isn't declared by a class the
 * mixin extends isn't remapped, so in production it overrides nothing (for example {@code getAddEntityPacket}, which is
 * {@code m_5654_} at runtime).
 * <p>
 * Usage: {@code AbstractMethodChecker [--mappings <tsrg>] <jar to check>...} with those jars and all their dependencies
 * on the classpath.
 */
public class AbstractMethodChecker {
    private static final Map<String, ClassNode> CACHE = new HashMap<>();
    private static final Set<String> MISSING = new TreeSet<>();
    private static int calls;
    private static int mixinMethods;
    /** class -> "name desc" of every method that has an intermediate (SRG) name */
    private static final Map<String, Set<String>> OBFUSCATED_METHODS = new HashMap<>();

    public static void main(final String[] rawArgs) throws IOException {
        final List<String> problems = new ArrayList<>();
        int checked = 0;

        final List<String> args = new ArrayList<>(List.of(rawArgs));
        final int mappingsIndex = args.indexOf("--mappings");
        if (mappingsIndex >= 0) {
            readMappings(Path.of(args.get(mappingsIndex + 1)));
            args.remove(mappingsIndex + 1);
            args.remove(mappingsIndex);
        }

        for (final String jarPath : args) {
            try (final JarFile jar = new JarFile(jarPath)) {
                for (final JarEntry entry : jar.stream().toList()) {
                    final String name = entry.getName();
                    if (!name.endsWith(".class") || name.startsWith("META-INF/") || name.equals("module-info.class")) {
                        continue;
                    }

                    final String className = name.substring(0, name.length() - ".class".length());
                    checkCalls(jar, entry, className, problems);
                    if (!OBFUSCATED_METHODS.isEmpty()) {
                        checkMixinOverrides(jar, entry, problems);
                    }

                    final ClassNode node = load(className);
                    if (node == null || (node.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE)) != 0) {
                        continue;
                    }

                    checked++;
                    check(node, problems);
                }
            }
        }

        if (!MISSING.isEmpty()) {
            System.out.println("Classes not on the classpath (their hierarchies were skipped): " + MISSING.size());
            MISSING.stream().limit(20).forEach(name -> System.out.println("  " + name));
        }

        System.out.println("Checked " + checked + " concrete classes and " + calls + " method calls"
                + (OBFUSCATED_METHODS.isEmpty() ? "" : " and " + mixinMethods + " mixin methods for missed overrides"));
        if (problems.isEmpty()) {
            System.out.println("No problems found");
            return;
        }

        problems.forEach(System.out::println);
        System.out.println(problems.size() + " problems");
        System.exit(1);
    }

    private static void check(final ClassNode node, final List<String> problems) {
        final Set<String> interfaces = new LinkedHashSet<>();
        for (ClassNode current = node; current != null; current = current.superName == null ? null : load(current.superName)) {
            current.interfaces.forEach(name -> collectInterfaces(name, interfaces));
        }

        for (final String interfaceName : interfaces) {
            final ClassNode interfaceNode = load(interfaceName);
            for (final MethodNode method : interfaceNode.methods) {
                if ((method.access & Opcodes.ACC_ABSTRACT) == 0 || (method.access & Opcodes.ACC_STATIC) != 0) {
                    continue;
                }

                if (!isImplemented(node, interfaces, method.name, method.desc)) {
                    problems.add(node.name + " does not implement " + interfaceName + "#" + method.name + method.desc);
                }
            }
        }
    }

    private static void readMappings(final Path tsrg) throws IOException {
        String currentClass = null;
        for (final String line : Files.readAllLines(tsrg)) {
            if (line.isEmpty() || line.startsWith("tsrg")) {
                continue;
            }
            if (!line.startsWith("\t")) {
                currentClass = line.split(" ")[0];
                continue;
            }
            final String[] parts = line.trim().split(" ");
            // methods are "name desc intermediate"; fields are "name intermediate"
            if (currentClass != null && parts.length == 3 && parts[1].startsWith("(") && !parts[0].equals(parts[2])) {
                OBFUSCATED_METHODS.computeIfAbsent(currentClass, k -> new java.util.HashSet<>()).add(parts[0] + " " + parts[1]);
            }
        }
    }

    /**
     * Reports mixin methods that match a Minecraft method of the target's hierarchy by dev name, but weren't remapped.
     */
    private static void checkMixinOverrides(final JarFile jar, final JarEntry entry, final List<String> problems) throws IOException {
        final ClassNode node = new ClassNode();
        try (final InputStream stream = jar.getInputStream(entry)) {
            new ClassReader(stream).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
        }

        final List<String> targets = mixinTargets(node);
        if (targets.isEmpty()) {
            return;
        }

        for (final MethodNode method : node.methods) {
            if ((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) != 0 || method.name.startsWith("<") || hasMixinAnnotation(method)) {
                continue;
            }
            mixinMethods++;

            for (final String target : targets) {
                final String owner = findObfuscatedDeclaration(target, method.name + " " + method.desc, new java.util.HashSet<>());
                if (owner != null) {
                    problems.add(node.name + "#" + method.name + method.desc + " looks like an override of " + owner
                            + " but wasn't remapped, so it overrides nothing in production. Make the mixin extend the class that declares it.");
                    break;
                }
            }
        }
    }

    private static List<String> mixinTargets(final ClassNode node) {
        final List<String> targets = new ArrayList<>();
        final List<AnnotationNode> annotations = new ArrayList<>();
        if (node.invisibleAnnotations != null) annotations.addAll(node.invisibleAnnotations);
        if (node.visibleAnnotations != null) annotations.addAll(node.visibleAnnotations);
        for (final AnnotationNode annotation : annotations) {
            if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;") || annotation.values == null) {
                continue;
            }
            for (int i = 0; i < annotation.values.size(); i += 2) {
                final Object value = annotation.values.get(i + 1);
                if (value instanceof final List<?> list) {
                    for (final Object element : list) {
                        if (element instanceof final Type type) {
                            targets.add(type.getInternalName());
                        } else if (element instanceof final String string && "targets".equals(annotation.values.get(i))) {
                            targets.add(string.replace('.', '/'));
                        }
                    }
                }
            }
        }
        return targets;
    }

    private static boolean hasMixinAnnotation(final MethodNode method) {
        final List<AnnotationNode> annotations = new ArrayList<>();
        if (method.invisibleAnnotations != null) annotations.addAll(method.invisibleAnnotations);
        if (method.visibleAnnotations != null) annotations.addAll(method.visibleAnnotations);
        for (final AnnotationNode annotation : annotations) {
            if (annotation.desc.startsWith("Lorg/spongepowered/asm/mixin/") || annotation.desc.startsWith("Lcom/llamalad7/mixinextras/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the class in {@code className}'s hierarchy that declares {@code nameAndDesc} under an intermediate name
     */
    private static String findObfuscatedDeclaration(final String className, final String nameAndDesc, final Set<String> visited) {
        if (!visited.add(className)) {
            return null;
        }
        final Set<String> obfuscated = OBFUSCATED_METHODS.get(className);
        if (obfuscated != null && obfuscated.contains(nameAndDesc)) {
            return className;
        }
        final ClassNode node = load(className);
        if (node == null) {
            return null;
        }
        final List<String> parents = new ArrayList<>(node.interfaces);
        if (node.superName != null) {
            parents.add(0, node.superName);
        }
        for (final String parent : parents) {
            final String found = findObfuscatedDeclaration(parent, nameAndDesc, visited);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Reports calls to methods that don't exist on the owner or anything it inherits from.
     */
    private static void checkCalls(final JarFile jar, final JarEntry entry, final String className, final List<String> problems) throws IOException {
        final ClassNode node = new ClassNode();
        try (final InputStream stream = jar.getInputStream(entry)) {
            new ClassReader(stream).accept(node, ClassReader.SKIP_FRAMES);
        }

        final Set<String> reported = new TreeSet<>();
        for (final MethodNode method : node.methods) {
            for (final AbstractInsnNode insn : method.instructions) {
                if (!(insn instanceof final MethodInsnNode call) || call.owner.startsWith("[")) {
                    continue;
                }

                final ClassNode owner = load(call.owner);
                if (owner == null || call.name.equals("<init>") && owner.methods.stream().anyMatch(m -> m.name.equals("<init>") && m.desc.equals(call.desc))) {
                    continue;
                }

                calls++;
                if (!resolves(owner, call.name, call.desc, new TreeSet<>()) && reported.add(call.owner + "#" + call.name + call.desc)) {
                    problems.add(className + " calls missing method " + call.owner + "#" + call.name + call.desc);
                }
            }
        }
    }

    /**
     * Method resolution as the JVM does it: the class, its superclasses, then every superinterface. Classes that aren't
     * on the classpath count as resolving, since they're reported separately.
     */
    private static boolean resolves(final ClassNode node, final String name, final String desc, final Set<String> visited) {
        if (!visited.add(node.name)) {
            return false;
        }

        for (final MethodNode method : node.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return true;
            }
        }

        // Signature-polymorphic methods (MethodHandle#invoke...) match any descriptor
        if (node.name.equals("java/lang/invoke/MethodHandle") || node.name.equals("java/lang/invoke/VarHandle")) {
            return true;
        }

        final List<String> parents = new ArrayList<>(node.interfaces);
        if (node.superName != null) {
            parents.add(0, node.superName);
        }

        for (final String parentName : parents) {
            final ClassNode parent = load(parentName);
            if (parent == null || resolves(parent, name, desc, visited)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isImplemented(final ClassNode node, final Set<String> interfaces, final String name, final String desc) {
        for (ClassNode current = node; current != null; current = current.superName == null ? null : load(current.superName)) {
            for (final MethodNode method : current.methods) {
                if (method.name.equals(name) && method.desc.equals(desc) && (method.access & Opcodes.ACC_ABSTRACT) == 0) {
                    return true;
                }
            }
        }

        for (final String interfaceName : interfaces) {
            for (final MethodNode method : load(interfaceName).methods) {
                if (method.name.equals(name) && method.desc.equals(desc) && (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_STATIC)) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void collectInterfaces(final String name, final Set<String> interfaces) {
        final ClassNode node = load(name);
        if (node == null || !interfaces.add(name)) {
            return;
        }

        node.interfaces.forEach(parent -> collectInterfaces(parent, interfaces));
    }

    private static ClassNode load(final String name) {
        if (CACHE.containsKey(name)) {
            return CACHE.get(name);
        }

        ClassNode node = null;
        try (final InputStream stream = ClassLoader.getSystemResourceAsStream(name + ".class")) {
            if (stream != null) {
                node = new ClassNode();
                new ClassReader(stream).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            } else {
                MISSING.add(name);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Couldn't read " + name, e);
        }

        CACHE.put(name, node);
        return node;
    }
}
