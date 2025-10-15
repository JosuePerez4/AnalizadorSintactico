import java.util.regex.*;
import java.util.*;

public class SintaxisAnalizer {
    // Patrones para cada componente del lenguaje
    private static final Pattern CLASE_PATTERN = Pattern.compile("(public\\s+)?class\\s+(\\w+)\\s*\\{");
    private static final Pattern METODO_PATTERN = Pattern.compile("(public|private|protected|void)?\\s*(static)?\\s*([\\w<>\\[\\]]+)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(?<!\\w)(public|private|protected)?\\s*([\\w\\[\\]]+(?:<[^>]+>)?)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=\\s*[^;]+)?;");
    private static final Pattern IF_PATTERN = Pattern.compile("if\\s*\\(([^)]+)\\)\\s*\\{");
    private static final Pattern ELSE_PATTERN = Pattern.compile("\\}\\s*else\\s*\\{");
    private static final Pattern FOR_PATTERN = Pattern.compile("for\\s*\\(([^)]+)\\)\\s*\\{");
    private static final Pattern WHILE_PATTERN = Pattern.compile("while\\s*\\(([^)]+)\\)\\s*\\{");

    // Palabras clave del modo español (subset de GLC propuesta)
    private static final String KW_SI = "si";
    private static final String KW_ENTONCES = "entonces";
    private static final String KW_SINO = "sino";
    private static final String KW_FINSI = "finsi";
    private static final String KW_MIENTRAS = "mientras";
    private static final String KW_HACER = "hacer";
    private static final String KW_FINMIENTRAS = "finmientras";

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
        
        // Modo: detectar español vs Java (heurística simple)
        if (esCodigoEspanol(codigoFuente)) {
            analizarProgramaEspanol(codigoFuente);
        } else {
            // Analizar el tipo de programa Java
            if (codigoFuente.contains("class")) {
                analizarClase(codigoFuente);
            } else if (codigoFuente.contains("(")) {
                analizarMetodo(codigoFuente);
            } else {
                analizarDeclaraciones(codigoFuente);
            }
        }
        
        return arbol.toString();
    }

    private boolean esCodigoEspanol(String codigo) {
        String lower = codigo.toLowerCase(Locale.ROOT);
        int hits = 0;
        if (lower.contains(KW_SI)) hits++;
        if (lower.contains(KW_ENTONCES)) hits++;
        if (lower.contains(KW_SINO)) hits++;
        if (lower.contains(KW_FINSI)) hits++;
        if (lower.contains(KW_MIENTRAS)) hits++;
        if (lower.contains(KW_HACER)) hits++;
        if (lower.contains(KW_FINMIENTRAS)) hits++;
        // Considerar español si hay al menos dos palabras clave del subset
        return hits >= 2 && !lower.contains("class") && !lower.contains("public") && !lower.contains("if (") && !lower.contains("while (");
    }

    // ============== Analizador simple para el subset en español ==============
    private void analizarProgramaEspanol(String codigo) {
        // Tokenizar por líneas para simular bloques con palabras de inicio/fin
        List<String> lineas = dividirEnLineasNoVacias(codigo);
        int index = 0;
        while (index < lineas.size()) {
            String linea = lineas.get(index).trim();
            if (linea.isEmpty()) { index++; continue; }
            if (empiezaConPalabra(linea, KW_MIENTRAS)) {
                index = analizarMientras(lineas, index);
                continue;
            }
            if (empiezaConPalabra(linea, KW_SI)) {
                index = analizarSi(lineas, index);
                continue;
            }
            // Asignación simple: identificador = expresión
            if (linea.matches("[a-zA-Z_][a-zA-Z0-9_]*\\s*=.+")) {
                agregarNodo("Asignación: " + linea);
                index++;
                continue;
            }
            // Si no reconocible, reportar como sentencia genérica
            agregarNodo("Sentencia: " + linea);
            index++;
        }
        indentacion--;
    }

    private int analizarMientras(List<String> lineas, int inicio) {
        String cabecera = lineas.get(inicio).trim();
        // Formato esperado: mientras CONDICION hacer
        agregarNodo("Mientras");
        indentacion++;
        String condicion = extraerEntrePalabras(cabecera, KW_MIENTRAS, KW_HACER);
        agregarNodo("Condición: " + (condicion.isEmpty() ? "<vacía>" : condicion.trim()));
        // Buscar bloque hasta finmientras
        int i = inicio + 1;
        agregarNodo("Bloque");
        indentacion++;
        while (i < lineas.size()) {
            String linea = lineas.get(i).trim();
            if (empiezaConPalabra(linea, KW_FINMIENTRAS)) {
                break;
            }
            // Reingresar para anidar estructuras
            if (empiezaConPalabra(linea, KW_SI)) {
                i = analizarSi(lineas, i);
                continue;
            }
            if (empiezaConPalabra(linea, KW_MIENTRAS)) {
                i = analizarMientras(lineas, i);
                continue;
            }
            if (linea.matches("[a-zA-Z_][a-zA-Z0-9_]*\\s*=.+")) {
                agregarNodo("Asignación: " + linea);
            } else {
                agregarNodo("Sentencia: " + linea);
            }
            i++;
        }
        indentacion--; // fin Bloque
        indentacion--; // fin Mientras
        return Math.min(i + 1, lineas.size());
    }

    private int analizarSi(List<String> lineas, int inicio) {
        String cabecera = lineas.get(inicio).trim();
        // Formato esperado: si CONDICION entonces
        agregarNodo("Si");
        indentacion++;
        String condicion = extraerEntrePalabras(cabecera, KW_SI, KW_ENTONCES);
        agregarNodo("Condición: " + (condicion.isEmpty() ? "<vacía>" : condicion.trim()));
        // then-bloque
        agregarNodo("Entonces");
        indentacion++;
        int i = inicio + 1;
        while (i < lineas.size()) {
            String linea = lineas.get(i).trim();
            if (empiezaConPalabra(linea, KW_SINO) || empiezaConPalabra(linea, KW_FINSI)) {
                break;
            }
            if (empiezaConPalabra(linea, KW_SI)) {
                i = analizarSi(lineas, i);
                continue;
            }
            if (empiezaConPalabra(linea, KW_MIENTRAS)) {
                i = analizarMientras(lineas, i);
                continue;
            }
            if (linea.matches("[a-zA-Z_][a-zA-Z0-9_]*\\s*=.+")) {
                agregarNodo("Asignación: " + linea);
            } else {
                agregarNodo("Sentencia: " + linea);
            }
            i++;
        }
        indentacion--; // fin entonces

        // else-bloque opcional
        if (i < lineas.size() && empiezaConPalabra(lineas.get(i).trim(), KW_SINO)) {
            agregarNodo("Sino");
            indentacion++;
            i++;
            while (i < lineas.size()) {
                String linea = lineas.get(i).trim();
                if (empiezaConPalabra(linea, KW_FINSI)) {
                    break;
                }
                if (empiezaConPalabra(linea, KW_SI)) {
                    i = analizarSi(lineas, i);
                    continue;
                }
                if (empiezaConPalabra(linea, KW_MIENTRAS)) {
                    i = analizarMientras(lineas, i);
                    continue;
                }
                if (linea.matches("[a-zA-Z_][a-zA-Z0-9_]*\\s*=.+")) {
                    agregarNodo("Asignación: " + linea);
                } else {
                    agregarNodo("Sentencia: " + linea);
                }
                i++;
            }
            indentacion--; // fin sino
        }
        indentacion--; // fin si
        // Consumir finsi si presente
        if (i < lineas.size() && empiezaConPalabra(lineas.get(i).trim(), KW_FINSI)) {
            i++;
        }
        return i;
    }

    private List<String> dividirEnLineasNoVacias(String codigo) {
        String[] raw = codigo.replace("\r", "").split("\n");
        List<String> out = new ArrayList<>();
        for (String s : raw) {
            out.add(s);
        }
        return out;
    }

    private boolean empiezaConPalabra(String linea, String palabra) {
        String l = linea.toLowerCase(Locale.ROOT);
        return l.startsWith(palabra + " ") || l.equals(palabra);
    }

    private String extraerEntrePalabras(String linea, String ini, String fin) {
        String l = linea.toLowerCase(Locale.ROOT);
        int i = l.indexOf(ini);
        int j = l.lastIndexOf(fin);
        if (i == -1 || j == -1 || j <= i) return "";
        return linea.substring(i + ini.length(), j);
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