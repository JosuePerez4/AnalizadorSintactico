import java.util.regex.*;

public class SintaxisAnalizer {
    // Patrones para cada componente del lenguaje
    private static final Pattern CLASE_PATTERN = Pattern.compile("(public\\s+)?class\\s+(\\w+)\\s*\\{");
    private static final Pattern METODO_PATTERN = Pattern.compile("(public|private|protected|void)?\\s*(static)?\\s*([\\w<>\\[\\]]+)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
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
    
    private void agregarDebug(String mensaje) {
        // Solo para debug - se puede comentar en producción
        // System.out.println("DEBUG: " + mensaje);
    }

    public String analizar(String codigoFuente) {
        if (codigoFuente == null || codigoFuente.isEmpty()) {
            return "Error: El código fuente está vacío.";
        }
        
        // Actualizar el código fuente de la instancia
        this.codigoFuente = codigoFuente;
        
        // Reinicializar el árbol y la indentación
        arbol = new StringBuilder("Árbol de derivación:\n");
        indentacion = 0;
        
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
            agregarDebug("Cuerpo de clase extraído: " + cuerpoClase.substring(0, Math.min(50, cuerpoClase.length())));
            analizarContenidoClase(cuerpoClase);
            indentacion--;
        } else {
            agregarNodo("Error: No se encontró una clase válida");
        }
    }

    private void analizarContenidoClase(String cuerpoClase) {
        agregarDebug("Analizando contenido de clase, longitud: " + cuerpoClase.length());
        
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
        int contadorMetodos = 0;
        
        while (metodoMatcher.find()) {
            contadorMetodos++;
            agregarDebug("Método encontrado #" + contadorMetodos + ": " + metodoMatcher.group(4));
            
            if (!tieneMetodos) {
                agregarNodo("Métodos");
                indentacion++;
                tieneMetodos = true;
            }
            // Analizar el método encontrado directamente
            agregarNodo("Método");
            indentacion++;
            agregarNodo("Tipo: " + metodoMatcher.group(3));
            agregarNodo("Nombre: " + metodoMatcher.group(4));
            agregarNodo("Parámetros: " + metodoMatcher.group(5));
            
            // Analizar cuerpo del método
            String cuerpoMetodo = extraerBloque(cuerpoClase, metodoMatcher.end());
            agregarDebug("Cuerpo del método extraído, longitud: " + cuerpoMetodo.length());
            analizarContenidoMetodo(cuerpoMetodo);
            indentacion--;
        }
        
        agregarDebug("Total de métodos encontrados: " + contadorMetodos);
        if (tieneMetodos) indentacion--;
        
        // Si no se encontraron métodos ni atributos, mostrar mensaje
        if (!tieneAtributos && !tieneMetodos) {
            agregarNodo("Clase vacía o sin elementos reconocibles");
        }
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
        int fin = codigo.length(); // Por defecto, hasta el final
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
        
        // Si no se encontró el cierre, usar hasta el final del código
        if (fin == inicio) {
            fin = codigo.length();
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

    public String mostrarCaracterPorCaracter() {
        StringBuilder resultado = new StringBuilder();
        resultado.append("Análisis carácter por carácter:\n");
        resultado.append("================================\n");
        
        for (int i = 0; i < codigoFuente.length(); i++) {
            char c = codigoFuente.charAt(i);
            
            // Omitir espacios en blanco
            if (Character.isWhitespace(c)) {
                continue;
            }
            
            String tipo = obtenerTipoCaracter(c);
            resultado.append(String.format("Posición %3d: '%c' (%s)\n", i, c, tipo));
        }
        
        return resultado.toString();
    }
    
    private String obtenerTipoCaracter(char c) {
        if (Character.isLetter(c)) {
            return "Letra";
        } else if (Character.isDigit(c)) {
            return "Dígito";
        } else if (Character.isWhitespace(c)) {
            if (c == ' ') return "Espacio";
            if (c == '\t') return "Tabulación";
            if (c == '\n') return "Salto de línea";
            if (c == '\r') return "Retorno de carro";
            return "Espacio en blanco";
        } else if (c == '{') {
            return "Llave de apertura";
        } else if (c == '}') {
            return "Llave de cierre";
        } else if (c == '(') {
            return "Paréntesis de apertura";
        } else if (c == ')') {
            return "Paréntesis de cierre";
        } else if (c == ';') {
            return "Punto y coma";
        } else if (c == '=') {
            return "Operador de asignación";
        } else if (c == '+' || c == '-' || c == '*' || c == '/') {
            return "Operador aritmético";
        } else if (c == '<' || c == '>' || c == '!' || c == '=') {
            return "Operador de comparación";
        } else if (c == '"') {
            return "Comilla doble";
        } else if (c == '\'') {
            return "Comilla simple";
        } else if (c == '.') {
            return "Punto";
        } else if (c == ',') {
            return "Coma";
        } else {
            return "Símbolo especial";
        }
    }
}