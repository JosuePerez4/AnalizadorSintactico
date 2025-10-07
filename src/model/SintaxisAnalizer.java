public class SintaxisAnalizer {
    private String codigoFuente;

    public SintaxisAnalizer(String codigoFuente) {
        this.codigoFuente = codigoFuente;
    }

    public String mostrarCaracterPorCaracter () {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < codigoFuente.length(); i++) {
            if (codigoFuente.charAt(i) !=' ') {
                resultado.append("Caracter ").append(+i + ": ").append(codigoFuente.charAt(i)).append("\n");
            }
        }
        return resultado.toString();
    }

    // Método para iniciar el análisis sintáctico
    public String analizar(String codigoFuente) {
        if (codigoFuente == null || codigoFuente.isEmpty()) {
            return "Error: El código fuente está vacío.";
        }

        // Aquí se realiza el análisis sintáctico
        // Llamamos al analizador que se encarga de verificar las reglas
        return analizarCodigo(codigoFuente);
    }

    private String analizarCodigo(String codigoFuente) {
        // A cómo lo pienso, se deberían cortar las palabras y analizarlas una por una
        String [] palabras = codigoFuente.split(" ");

        for (String palabra : palabras) {
            System.out.println(palabra);
        }

        if (codigoFuente.contains("class")) {
            return "El código parece ser una clase.";
        } else if (codigoFuente.contains("void")) {
            return "El código parece ser un método.";
        } else {
            return "El código no sigue una estructura reconocida.";
        }
    }
}
