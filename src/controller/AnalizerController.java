public class AnalizerController {
    private SintaxisAnalizer modelo;

    public AnalizerController(SintaxisAnalizer modelo) {
        this.modelo = modelo;
    }

    // Método para iniciar el análisis y retornar el resultado
    public String iniciarAnalisis(String codigoFuente) {
        modelo = new SintaxisAnalizer(codigoFuente);
        return modelo.analizar(codigoFuente);
    }
    
    // Método para obtener el análisis carácter por carácter
    public String obtenerAnalisisCaracteres(String codigoFuente) {
        modelo = new SintaxisAnalizer(codigoFuente);
        return modelo.mostrarCaracterPorCaracter();
    }
    
    // Método para obtener el árbol de derivación
    public String obtenerArbolDerivacion(String codigoFuente) {
        modelo = new SintaxisAnalizer(codigoFuente);
        return modelo.analizar(codigoFuente);
    }
}
