import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnalizerView extends JFrame {
    private AnalizerController controlador;
    
    // Componentes de la interfaz
    private JTextArea areaEntrada;
    private JTextArea areaCaracteres;
    private JTextArea areaArbol;
    private JButton botonAnalizar;
    private JButton botonLimpiar;
    
    public AnalizerView(AnalizerController controlador) {
        this.controlador = controlador;
        inicializarInterfaz();
    }
    
    private void inicializarInterfaz() {
        setTitle("Analizador Sintáctico - Java");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Configurar el look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Panel principal con scroll
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        
        // Panel superior para entrada de código
        JPanel panelEntrada = crearPanelEntrada();
        
        // Panel central con dos áreas de resultados
        JPanel panelResultados = crearPanelResultados();
        
        // Panel inferior con botones
        JPanel panelBotones = crearPanelBotones();
        
        // Agregar componentes al panel principal
        panelPrincipal.add(panelEntrada, BorderLayout.NORTH);
        panelPrincipal.add(panelResultados, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        
        // Agregar scroll al panel principal
        JScrollPane scrollPrincipal = new JScrollPane(panelPrincipal);
        scrollPrincipal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPrincipal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPrincipal, BorderLayout.CENTER);
        
        // Configurar la ventana
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private JPanel crearPanelEntrada() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Código Java a Analizar"));
        panel.setPreferredSize(new Dimension(0, 200));
        
        areaEntrada = new JTextArea(8, 50);
        areaEntrada.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaEntrada.setLineWrap(true);
        areaEntrada.setWrapStyleWord(true);
        areaEntrada.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Texto de ejemplo
        areaEntrada.setText("public class Ejemplo {\n" +
                           "    private int numero = 10;\n" +
                           "    \n" +
                           "    public void metodo() {\n" +
                           "        if (numero > 5) {\n" +
                           "            System.out.println(\"Mayor que 5\");\n" +
                           "        }\n" +
                           "    }\n" +
                           "}");
        
        JScrollPane scrollEntrada = new JScrollPane(areaEntrada);
        scrollEntrada.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollEntrada.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(scrollEntrada, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel crearPanelResultados() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(new TitledBorder("Resultados del Análisis"));
        
        // Panel izquierdo - Análisis de caracteres
        JPanel panelCaracteres = new JPanel(new BorderLayout());
        panelCaracteres.setBorder(new TitledBorder("Análisis Carácter por Carácter"));
        
        areaCaracteres = new JTextArea();
        areaCaracteres.setFont(new Font("Consolas", Font.PLAIN, 11));
        areaCaracteres.setEditable(false);
        areaCaracteres.setBackground(new Color(248, 248, 248));
        areaCaracteres.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollCaracteres = new JScrollPane(areaCaracteres);
        scrollCaracteres.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollCaracteres.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        panelCaracteres.add(scrollCaracteres, BorderLayout.CENTER);
        
        // Panel derecho - Árbol de derivación
        JPanel panelArbol = new JPanel(new BorderLayout());
        panelArbol.setBorder(new TitledBorder("Árbol de Derivación"));
        
        areaArbol = new JTextArea();
        areaArbol.setFont(new Font("Consolas", Font.PLAIN, 11));
        areaArbol.setEditable(false);
        areaArbol.setBackground(new Color(248, 248, 248));
        areaArbol.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollArbol = new JScrollPane(areaArbol);
        scrollArbol.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollArbol.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        panelArbol.add(scrollArbol, BorderLayout.CENTER);
        
        // Agregar paneles al panel principal
        panel.add(panelCaracteres);
        panel.add(panelArbol);
        
        return panel;
    }
    
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        botonAnalizar = new JButton("Analizar Código");
        botonAnalizar.setFont(new Font("Arial", Font.BOLD, 12));
        botonAnalizar.setBackground(new Color(70, 130, 180));
        botonAnalizar.setForeground(Color.WHITE);
        botonAnalizar.setPreferredSize(new Dimension(150, 35));
        botonAnalizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analizarCodigo();
            }
        });
        
        botonLimpiar = new JButton("Limpiar");
        botonLimpiar.setFont(new Font("Arial", Font.BOLD, 12));
        botonLimpiar.setBackground(new Color(220, 20, 60));
        botonLimpiar.setForeground(Color.WHITE);
        botonLimpiar.setPreferredSize(new Dimension(100, 35));
        botonLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiarResultados();
            }
        });
        
        panel.add(botonAnalizar);
        panel.add(botonLimpiar);
        
        return panel;
    }
    
    private void analizarCodigo() {
        String codigo = areaEntrada.getText().trim();
        
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, ingrese código Java para analizar.", 
                "Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Análisis de caracteres
            String analisisCaracteres = controlador.obtenerAnalisisCaracteres(codigo);
            areaCaracteres.setText(analisisCaracteres);
            
            // Árbol de derivación
            String arbolDerivacion = controlador.obtenerArbolDerivacion(codigo);
            areaArbol.setText(arbolDerivacion);
            
            // Mostrar mensaje de éxito
            JOptionPane.showMessageDialog(this, 
                "Análisis completado exitosamente.", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error durante el análisis: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void limpiarResultados() {
        areaCaracteres.setText("");
        areaArbol.setText("");
        areaEntrada.setText("");
    }
    
    public void mostrar() {
        setVisible(true);
    }
    
    public static void main(String[] args) {
        // Configurar el look and feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Crear y mostrar la interfaz
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AnalizerController controlador = new AnalizerController(null);
                AnalizerView vista = new AnalizerView(controlador);
                vista.mostrar();
            }
        });
    }
}
