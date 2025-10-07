public class AnalizerController {
    private SintaxisAnalizer modelo;

    public AnalizerController(SintaxisAnalizer modelo) {
        this.modelo = modelo;
    }

    // Método para iniciar el análisis y retornar el resultado
    public String iniciarAnalisis(String codigoFuente) {
        modelo = new SintaxisAnalizer(codigoFuente);
        return modelo.analizar();
    }
}
