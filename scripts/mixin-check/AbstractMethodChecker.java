import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
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
 * Usage: {@code AbstractMethodChecker <jar to check>...} with those jars and all their dependencies on the classpath.
 */
public class AbstractMethodChecker {
    private static final Map<String, ClassNode> CACHE = new HashMap<>();
    private static final Set<String> MISSING = new TreeSet<>();

    public static void main(final String[] args) throws IOException {
        final List<String> problems = new ArrayList<>();
        int checked = 0;

        for (final String jarPath : args) {
            try (final JarFile jar = new JarFile(jarPath)) {
                for (final JarEntry entry : jar.stream().toList()) {
                    final String name = entry.getName();
                    if (!name.endsWith(".class") || name.startsWith("META-INF/") || name.equals("module-info.class")) {
                        continue;
                    }

                    final ClassNode node = load(name.substring(0, name.length() - ".class".length()));
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

        System.out.println("Checked " + checked + " concrete classes");
        if (problems.isEmpty()) {
            System.out.println("No problems found");
            return;
        }

        problems.forEach(System.out::println);
        System.out.println(problems.size() + " unimplemented interface methods");
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
