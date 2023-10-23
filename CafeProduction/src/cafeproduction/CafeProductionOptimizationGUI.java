
/**
 * Café Production Optimization GUI
 *
 * Esta aplicación de Java proporciona una interfaz gráfica para optimizar la producción de café
 * mediante la asignación óptima de recursos a tareas.
 *
 * Utiliza la biblioteca Apache Commons Math 3 para realizar la optimización no lineal.
 */
package cafeproduction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.*;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Clase CafeProductionOptimizationGUI
 *
 * Esta clase define la interfaz gráfica de la aplicación y maneja la optimización de la producción de café.
 */
public class CafeProductionOptimizationGUI extends JFrame {

    // Componentes de la interfaz de usuario
    private JTextField[] executionTimeFields;
    private JTextField[] resourceConstraintFields;
    private JButton optimizeButton;
    private JTextArea resultTextArea;

    /**
     * Constructor de la clase
     *
     * Configura la interfaz gráfica de la aplicación.
     */
    public CafeProductionOptimizationGUI() {
        setTitle("Optimización de Café");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2));

        // Etiquetas para ingresar datos
        JLabel[] labels = {
            new JLabel("Tarea 1 (minutos por kilo):"),
            new JLabel("Tarea 2 (minutos por kilo):"),
            new JLabel("Tarea 3 (minutos por kilo):"),
            new JLabel("Tarea 4 (minutos por kilo):"),
            new JLabel("Tarea 5 (minutos por kilo):"),
            new JLabel("Recurso 1 (disponibilidad):"),
            new JLabel("Recurso 2 (disponibilidad):"),
            new JLabel("Recurso 3 (disponibilidad):")
        };

        // Campos de entrada para tiempos de ejecución y restricciones de recursos
        executionTimeFields = new JTextField[5];
        resourceConstraintFields = new JTextField[3];

        for (int i = 0; i < 5; i++) {
            executionTimeFields[i] = new JTextField();
        }

        for (int i = 0; i < 3; i++) {
            resourceConstraintFields[i] = new JTextField();
        }

        // Área de resultados
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);

        // Botón de optimización
        optimizeButton = new JButton("Optimizar");
        optimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optimizeCafeProduction();
            }
        });

        // Agrega componentes a la interfaz
        for (int i = 0; i < 5; i++) {
            panel.add(labels[i]);
            panel.add(executionTimeFields[i]);
        }

        for (int i = 0; i < 3; i++) {
            panel.add(labels[i + 5]);
            panel.add(resourceConstraintFields[i]);
        }

        panel.add(optimizeButton);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
    }

    /**
     * Método optimizeCafeProduction
     *
     * Realiza la optimización de la producción de café utilizando datos ingresados por el usuario.
     */
    public void optimizeCafeProduction() {
        // Obtén los tiempos de ejecución ingresados por el usuario
        double[] executionTimes = new double[5];
        for (int i = 0; i < 5; i++) {
            try {
                executionTimes[i] = Double.parseDouble(executionTimeFields[i].getText());
            } catch (NumberFormatException e) {
                // Manejar error de entrada no válida
                resultTextArea.setText("Error: Ingresa tiempos de ejecución válidos.");
                return;
            }
            if (executionTimes[i] <= 0) {
                // Manejar error de tiempos de ejecución no positivos
                resultTextArea.setText("Error: Los tiempos de ejecución deben ser positivos.");
                return;
            }
        }

        // Obtén las restricciones de recursos ingresadas por el usuario
        double[] resourceConstraints = new double[3];
        for (int i = 0; i < 3; i++) {
            try {
                resourceConstraints[i] = Double.parseDouble(resourceConstraintFields[i].getText());
            } catch (NumberFormatException e) {
                // Manejar error de entrada no válida
                resultTextArea.setText("Error: Ingresa restricciones de recursos válidas.");
                return;
            }
            if (resourceConstraints[i] < 0) {
                // Manejar error de restricciones de recursos negativas
                resultTextArea.setText("Error: Las restricciones de recursos deben ser no negativas.");
                return;
            }
        }

        // Define la función objetivo no lineal
        MultivariateFunction objectiveFunction = new MultivariateFunction() {
            @Override
            public double value(double[] x) {
                double totalExecutionTime = 0;
                for (int i = 0; i < x.length; i++) {
                    totalExecutionTime += executionTimes[i] * x[i];
                }
                return totalExecutionTime;
            }
        };

        // Configura el optimizador SimplexSolver
        SimplexSolver optimizer = new SimplexSolver();

        // Define el punto de inicio
        double[] initialGuess = new double[5];
        for (int i = 0; i < 5; i++) {
            initialGuess[i] = 1.0;  // Inicializa con asignaciones igualmente distribuidas
        }

        // Configura las restricciones
        LinearConstraintSet constraints = new LinearConstraintSet(
            new LinearConstraint(new double[]{1, 0, 0, 0, 0}, Relationship.LEQ, resourceConstraints[0]),
            new LinearConstraint(new double[]{0, 1, 0, 0, 0}, Relationship.LEQ, resourceConstraints[1]),
            new LinearConstraint(new double[]{0, 0, 1, 0, 0}, Relationship.LEQ, resourceConstraints[2])
        );

        // Realiza la optimización
        PointValuePair solution = null;
        solution = optimizer.optimize(new MaxIter(100), new ObjectiveFunction(objectiveFunction), GoalType.MINIMIZE, new InitialGuess(initialGuess), constraints);

        // Configura el optimizador CMA-ES (Covariance Matrix Adaptation Evolution Strategy)
        RandomGenerator randomGenerator = new JDKRandomGenerator();
        randomGenerator.setSeed(12345); // Puedes elegir una semilla de tu elección

        double[] resourceAllocation = solution.getPoint();
        double minExecutionTime = solution.getValue();

        // Muestra la asignación óptima de recursos y el tiempo total de ejecución mínimo
        resultTextArea.setText("Asignación óptima de recursos:\n");
        for (int i = 0; i < resourceAllocation.length; i++) {
            resultTextArea.append("Tarea " + (i + 1) + ": " + resourceAllocation[i] + "\n");
        }
        resultTextArea.append("Tiempo total de ejecución mínimo: " + minExecutionTime + " minutos por kilo");
    }

    /**
     * Método main
     *
     * Punto de entrada de la aplicación. Crea una instancia de la interfaz gráfica y la muestra.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CafeProductionOptimizationGUI().setVisible(true);
            }
        });
    }
}
