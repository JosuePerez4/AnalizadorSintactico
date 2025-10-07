public class SintaxisAnalizer {
    private String codigoFuente;

    public SintaxisAnalizer(String codigoFuente) {
        this.codigoFuente = codigoFuente;
    }

    // Método para iniciar el análisis sintáctico
    public String analizar() {
        if (codigoFuente == null || codigoFuente.isEmpty()) {
            return "Error: El código fuente está vacío.";
        }

        // Aquí se realiza el análisis sintáctico
        // Llamamos al analizador que se encarga de verificar las reglas
        return analizarCodigo();
    }

    private String analizarCodigo() {
        // Esto sería donde agregamos la lógica de parsing,
        // por ejemplo, dividiendo el código y verificando las reglas.
        
        // Aquí por simplicidad solo se retornan ejemplos de reglas.
        if (codigoFuente.contains("class")) {
            return "El código parece ser una clase.";
        } else if (codigoFuente.contains("void")) {
            return "El código parece ser un método.";
        } else {
            return "El código no sigue una estructura reconocida.";
        }
    }
}
