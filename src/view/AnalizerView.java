import java.util.Scanner;

public class AnalizerView {
    private AnalizerController controlador;

    public AnalizerView(AnalizerController controlador) {
        this.controlador = controlador;
    }

    public static void main(String[] args) {
        AnalizerController controlador = new AnalizerController(null);
        AnalizerView vista = new AnalizerView(controlador);
        vista.mostrar();
    }

    // Método para mostrar la interfaz de usuario
    public void mostrar() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Introduce el fragmento de código Java a analizar:");
        String codigo = scanner.nextLine();
        
        // Solicita al controlador que inicie el análisis
        String resultado = controlador.iniciarAnalisis(codigo);
        
        // Muestra el resultado del análisis
        System.out.println("Resultado del análisis sintáctico:");
        System.out.println(resultado);

        // Muestra cada carácter del código fuente
        /*System.out.println("Análisis carácter por carácter:");
        SintaxisAnalizer analizador = new SintaxisAnalizer(codigo);
        System.out.println(analizador.mostrarCaracterPorCaracter());*/
    }
}
