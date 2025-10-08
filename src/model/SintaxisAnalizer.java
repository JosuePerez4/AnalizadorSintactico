import java.util.regex.*;
import java.util.*;

public class SintaxisAnalizer {
    private String codigoFuente;

    public SintaxisAnalizer(String codigoFuente) {
        this.codigoFuente = codigoFuente;
    }

    public String mostrarCaracterPorCaracter () {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < codigoFuente.length(); i++) {
            if (codigoFuente.charAt(i) !=' ') {
                resultado.append("Caracter ").append(i).append(": ").append(codigoFuente.charAt(i)).append("\n");
            }
        }
        return resultado.toString();
    }

    // Método para iniciar el análisis sintáctico
    public String analizar(String codigoFuente) {
        if (codigoFuente == null || codigoFuente.isEmpty()) {
            return "Error: El código fuente está vacío.";
        }
        return analizarCodigo(codigoFuente);
    }

    private String analizarCodigo(String codigoFuente) {
        // Reglas básicas para clase, método y variables en Java
        Pattern clasePattern = Pattern.compile("class\\s+(\\w+)\\s*\\{");
        Pattern metodoPattern = Pattern.compile("(public|private|protected)?\\s*(static)?\\s*([\\w<>\\[\\]]+)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
        // Variable: tipo nombre [= valor]; (mejorado para evitar < y > como nombre)
        Pattern variablePattern = Pattern.compile(
            "(?<!\\w)(public|private|protected)?\\s*([\\w\\[\\]]+(?:<[^>]+>)?)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=\\s*[^;]+)?;"
        );

        Matcher claseMatcher = clasePattern.matcher(codigoFuente);
        Matcher metodoMatcher = metodoPattern.matcher(codigoFuente);

        StringBuilder arbol = new StringBuilder("Árbol de derivación:\n");

        boolean esClase = false;
        if (claseMatcher.find()) {
            esClase = true;
            String nombreClase = claseMatcher.group(1);
            arbol.append("Clase\n");
            arbol.append(" └── NombreClase: ").append(nombreClase).append("\n");
        }

        // Detectar atributos de clase (variables fuera de métodos)
        List<String> atributos = new ArrayList<>();
        // Extraer el bloque de la clase (entre { ... })
        int claseInicio = codigoFuente.indexOf('{');
        int claseFin = codigoFuente.lastIndexOf('}');
        String cuerpoClase = (claseInicio >= 0 && claseFin > claseInicio) ? codigoFuente.substring(claseInicio + 1, claseFin) : codigoFuente;

        // Buscar variables fuera de métodos (atributos)
        Matcher atributoMatcher = variablePattern.matcher(cuerpoClase);
        while (atributoMatcher.find()) {
            String tipo = atributoMatcher.group(2);
            String nombre = atributoMatcher.group(3);
            String valor = atributoMatcher.group(4);
            // Evitar variables dentro de métodos (muy básico: no dentro de llaves anidadas)
            if (!estaDentroDeMetodo(cuerpoClase, atributoMatcher.start())) {
                arbol.append("Atributo\n");
                arbol.append(" ├── Tipo: ").append(tipo).append("\n");
                arbol.append(" ├── Nombre: ").append(nombre).append("\n");
                arbol.append(" └── Valor: ").append(valor != null ? valor.replaceFirst("=\\s*", "") : "Sin valor").append("\n");
                atributos.add(nombre);
            }
        }

        List<String> metodos = new ArrayList<>();
        // Buscar métodos y variables locales dentro de cada método
        while (metodoMatcher.find()) {
            String tipo = metodoMatcher.group(3);
            String nombreMetodo = metodoMatcher.group(4);
            String parametros = metodoMatcher.group(5);

            arbol.append("Método\n");
            arbol.append(" ├── TipoRetorno: ").append(tipo).append("\n");
            arbol.append(" ├── NombreMetodo: ").append(nombreMetodo).append("\n");
            arbol.append(" └── Parámetros: ").append(parametros.isEmpty() ? "Sin parámetros" : parametros).append("\n");
            metodos.add(nombreMetodo);

            // Extraer el bloque del método
            int metodoInicio = metodoMatcher.end();
            int metodoFin = encontrarFinDeBloque(codigoFuente, metodoInicio - 1);
            if (metodoFin > metodoInicio) {
                String cuerpoMetodo = codigoFuente.substring(metodoInicio, metodoFin);
                Matcher variableLocalMatcher = variablePattern.matcher(cuerpoMetodo);
                while (variableLocalMatcher.find()) {
                    String tipoVar = variableLocalMatcher.group(2);
                    String nombreVar = variableLocalMatcher.group(3);
                    String valorVar = variableLocalMatcher.group(4);
                    arbol.append("  VariableLocal\n");
                    arbol.append("   ├── Tipo: ").append(tipoVar).append("\n");
                    arbol.append("   ├── Nombre: ").append(nombreVar).append("\n");
                    arbol.append("   └── Valor: ").append(valorVar != null ? valorVar.replaceFirst("=\\s*", "") : "Sin valor").append("\n");
                }
            }
        }

        if (esClase && (!metodos.isEmpty() || !atributos.isEmpty())) {
            return arbol.toString();
        } else if (esClase) {
            return arbol.toString() + "La clase no contiene métodos ni atributos detectados.\n";
        } else if (!metodos.isEmpty()) {
            return arbol.toString();
        } else {
            return "El código no sigue una estructura reconocida de clase, método o variable en Java.";
        }
    }

    // Ayuda: verifica si la posición está dentro de un método (muy básico)
    private boolean estaDentroDeMetodo(String cuerpoClase, int pos) {
        int nivel = 0;
        for (int i = 0; i < pos; i++) {
            char c = cuerpoClase.charAt(i);
            if (c == '{') nivel++;
            if (c == '}') nivel--;
        }
        return nivel > 0;
    }

    // Ayuda: encuentra el fin del bloque de llaves desde una posición inicial
    private int encontrarFinDeBloque(String texto, int inicio) {
        int nivel = 0;
        for (int i = inicio; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '{') nivel++;
            if (c == '}') {
                nivel--;
                if (nivel == 0) return i;
            }
        }
        return texto.length();
    }
}