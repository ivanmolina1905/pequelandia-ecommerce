import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.tools.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Extrae posiciones sintácticas sin resolver dependencias ni ejecutar el proyecto. */
public class DocScan {
    static String b64(String s) { return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }
    public static void main(String[] args) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        try (var fm = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            var paths = new ArrayList<Path>();
            try (var walk = Files.walk(Path.of(args[0], "src"))) {
                walk.filter(p -> p.toString().endsWith(".java")).sorted().forEach(paths::add);
            }
            var task = (JavacTask) compiler.getTask(null, fm, null, List.of("-proc:none"), null,
                    fm.getJavaFileObjectsFromPaths(paths));
            var units = task.parse();
            var trees = Trees.instance(task);
            for (var unit : units) {
                var pos = trees.getSourcePositions();
                String file = Path.of(unit.getSourceFile().toUri()).toString();
                String source = Files.readString(Path.of(file));
                new TreePathScanner<Void, Void>() {
                    @Override public Void scan(Tree node, Void v) {
                        if (node == null) return null;
                        long start = pos.getStartPosition(unit, node), end = pos.getEndPosition(unit, node);
                        String name = "", type = "", category = "";
                        if (node instanceof ClassTree t) { category = "TYPE"; name = t.getSimpleName().toString(); type = t.getKind().name(); }
                        else if (node instanceof MethodTree t) { category = "METHOD"; name = t.getName().toString(); type = String.valueOf(t.getReturnType()); }
                        else if (node instanceof VariableTree t) { category = "VARIABLE"; name = t.getName().toString(); type = String.valueOf(t.getType()); }
                        else if (node instanceof StatementTree && !(node instanceof BlockTree)) category = "STATEMENT";
                        if (!category.isEmpty() && start >= 0 && end >= start) {
                            String owner = "";
                            var path = getCurrentPath();
                            if (path != null) owner = path.getLeaf().getKind().name();
                            System.out.println(String.join("\t", b64(file), category, node.getKind().name(),
                                    Long.toString(start), Long.toString(end), Long.toString(unit.getLineMap().getLineNumber(start)),
                                    b64(name), b64(type), owner, b64(source.substring((int)start, (int)end))));
                        }
                        return super.scan(node, v);
                    }
                }.scan(unit, null);
            }
        }
    }
}
