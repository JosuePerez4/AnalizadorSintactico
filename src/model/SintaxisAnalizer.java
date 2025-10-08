import java.util.regex.*;
import java.util.*;

public class SintaxisAnalizer {
    // Patrones para cada componente del lenguaje
    private static final Pattern CLASE_PATTERN = Pattern.compile("(public\\s+)?class\\s+(\\w+)\\s*\\{");
    private static final Pattern METODO_PATTERN = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*([\\w<>\\[\\]]+)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(?<!\\w)(public|private|protected)?\\s*([\\w\\[\\]]+(?:<[^>]+>)?)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=\\s*[^;]+)?;");
    private static final Pattern IF_PATTERN = Pattern.compile("if\\s*\\(([^)]+)\\)\\s*\\{");
    private static final Pattern ELSE_PATTERN = Pattern.compile("\\}\\s*else\\s*\\{");
    private static final Pattern FOR_PATTERN = Pattern.compile("for\\s*\\(([^)]+)\\)\\s*\\{");
    private static final Pattern WHILE_PATTERN = Pattern.compile("while\\s*\\(([^)]+)\\)\\s*\\{");

    private String codigoFuente;
    private StringBuilder arbol;
    private int indentacion;

    public SintaxisAnalizer(String codigoFuente) {
        this.codigoFuente = codigoFuente;
        this.arbol = new StringBuilder();
        this.indentacion = 0;
    }

    private void agregarNodo(String texto) {
        arbol.append("  ".repeat(indentacion)).append(texto).append("\n");
    }

    public String analizar(String codigoFuente) {
        if (codigoFuente == null || codigoFuente.isEmpty()) {
            return "Error: El código fuente está vacío.";
        }
        
        arbol = new StringBuilder("Árbol de derivación:\n");
        agregarNodo("Programa");
        indentacion++;
        
        // Analizar el tipo de programa
        if (codigoFuente.contains("class")) {
            analizarClase(codigoFuente);
        } else if (codigoFuente.contains("(")) {
            analizarMetodo(codigoFuente);
        } else {
            analizarDeclaraciones(codigoFuente);
        }
        
        return arbol.toString();
    }

    private void analizarClase(String codigo) {
        Matcher claseMatcher = CLASE_PATTERN.matcher(codigo);
        if (claseMatcher.find()) {
            agregarNodo("Clase");
            indentacion++;
            agregarNodo("Nombre: " + claseMatcher.group(2));
            
            // Analizar el cuerpo de la clase
            String cuerpoClase = extraerBloque(codigo, claseMatcher.end());
            analizarContenidoClase(cuerpoClase);
            indentacion--;
        }
    }

    private void analizarContenidoClase(String cuerpoClase) {
        // Analizar atributos
        Matcher variableMatcher = VARIABLE_PATTERN.matcher(cuerpoClase);
        boolean tieneAtributos = false;
        while (variableMatcher.find()) {
            if (!estaDentroDeMetodo(cuerpoClase, variableMatcher.start())) {
                if (!tieneAtributos) {
                    agregarNodo("Atributos");
                    indentacion++;
                    tieneAtributos = true;
                }
                analizarVariable(variableMatcher);
            }
        }
        if (tieneAtributos) indentacion--;

        // Analizar métodos
        Matcher metodoMatcher = METODO_PATTERN.matcher(cuerpoClase);
        boolean tieneMetodos = false;
        while (metodoMatcher.find()) {
            if (!tieneMetodos) {
                agregarNodo("Métodos");
                indentacion++;
                tieneMetodos = true;
            }
            analizarMetodo(cuerpoClase.substring(metodoMatcher.start()));
        }
        if (tieneMetodos) indentacion--;
    }

    private void analizarMetodo(String codigo) {
        Matcher metodoMatcher = METODO_PATTERN.matcher(codigo);
        if (metodoMatcher.find()) {
            agregarNodo("Método");
            indentacion++;
            agregarNodo("Tipo: " + metodoMatcher.group(3));
            agregarNodo("Nombre: " + metodoMatcher.group(4));
            agregarNodo("Parámetros: " + metodoMatcher.group(5));
            
            // Analizar cuerpo del método
            String cuerpoMetodo = extraerBloque(codigo, metodoMatcher.end());
            analizarContenidoMetodo(cuerpoMetodo);
            indentacion--;
        }
    }

    private void analizarContenidoMetodo(String cuerpoMetodo) {
        // Analizar variables locales
        analizarDeclaraciones(cuerpoMetodo);
        
        // Analizar estructuras de control
        analizarEstructurasControl(cuerpoMetodo);
    }

    private void analizarDeclaraciones(String codigo) {
        Matcher variableMatcher = VARIABLE_PATTERN.matcher(codigo);
        boolean tieneVariables = false;
        while (variableMatcher.find()) {
            if (!tieneVariables) {
                agregarNodo("Variables");
                indentacion++;
                tieneVariables = true;
            }
            analizarVariable(variableMatcher);
        }
        if (tieneVariables) indentacion--;
    }

    private void analizarVariable(Matcher matcher) {
        agregarNodo("Variable");
        indentacion++;
        agregarNodo("Tipo: " + matcher.group(2));
        agregarNodo("Nombre: " + matcher.group(3));
        String valor = matcher.group(4);
        agregarNodo("Valor: " + (valor != null ? valor.replaceFirst("=\\s*", "") : "Sin valor"));
        indentacion--;
    }

    private void analizarEstructurasControl(String codigo) {
        analizarIf(codigo);
        analizarFor(codigo);
        analizarWhile(codigo);
    }

    private void analizarIf(String codigo) {
        Matcher ifMatcher = IF_PATTERN.matcher(codigo);
        while (ifMatcher.find()) {
            agregarNodo("If");
            indentacion++;
            agregarNodo("Condición: " + ifMatcher.group(1));
            String bloqueIf = extraerBloque(codigo, ifMatcher.end());
            analizarContenidoMetodo(bloqueIf);
            
            // Buscar else
            int finIf = ifMatcher.end() + bloqueIf.length();
            Matcher elseMatcher = ELSE_PATTERN.matcher(codigo.substring(finIf));
            if (elseMatcher.find()) {
                agregarNodo("Else");
                indentacion++;
                String bloqueElse = extraerBloque(codigo, finIf + elseMatcher.end());
                analizarContenidoMetodo(bloqueElse);
                indentacion--;
            }
            indentacion--;
        }
    }

    private void analizarFor(String codigo) {
        Matcher forMatcher = FOR_PATTERN.matcher(codigo);
        while (forMatcher.find()) {
            agregarNodo("For");
            indentacion++;
            agregarNodo("Condición: " + forMatcher.group(1));
            String bloqueFor = extraerBloque(codigo, forMatcher.end());
            analizarContenidoMetodo(bloqueFor);
            indentacion--;
        }
    }

    private void analizarWhile(String codigo) {
        Matcher whileMatcher = WHILE_PATTERN.matcher(codigo);
        while (whileMatcher.find()) {
            agregarNodo("While");
            indentacion++;
            agregarNodo("Condición: " + whileMatcher.group(1));
            String bloqueWhile = extraerBloque(codigo, whileMatcher.end());
            analizarContenidoMetodo(bloqueWhile);
            indentacion--;
        }
    }

    private String extraerBloque(String codigo, int inicio) {
        int nivel = 0;
        int fin = inicio;
        boolean encontroInicio = false;
        
        for (int i = inicio; i < codigo.length(); i++) {
            char c = codigo.charAt(i);
            if (c == '{') {
                nivel++;
                encontroInicio = true;
            }
            if (c == '}') {
                nivel--;
                if (nivel == 0 && encontroInicio) {
                    fin = i;
                    break;
                }
            }
        }
        return codigo.substring(inicio, fin);
    }

    private boolean estaDentroDeMetodo(String codigo, int pos) {
        int nivel = 0;
        for (int i = 0; i < pos; i++) {
            char c = codigo.charAt(i);
            if (c == '{') nivel++;
            if (c == '}') nivel--;
        }
        return nivel > 0;
    }
}