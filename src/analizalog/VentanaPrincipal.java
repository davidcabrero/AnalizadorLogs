/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package analizalog;

import java.awt.Color;
import java.awt.Dimension;
import static java.awt.PageAttributes.ColorType.COLOR;
import java.awt.Toolkit;
import static java.awt.event.KeyEvent.VK_ENTER;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.scene.paint.Color.color;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.Renderer;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author David Cabrero
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    /**
     * Creates new form VentanaPrincipal
     */
    public VentanaPrincipal() {
        initComponents();
        setIconImage(new ImageIcon(getClass().getResource("logo.png")).getImage());
    }

    public static File directorio;
    public static String nombreLog;
    public static String nombreError;
    ArrayList<Integer> busquedas = new ArrayList<Integer>();
    int lastElement = 0;
    int filaBusqueda = 0;
    public static boolean conexionRealizada = false;
    public static boolean pulsarFila = true;

    /**
     * Obtiene el nombre de los logs y archivos ERROR a analizar según la fecha
     * que introduce el usuario.
     */
    private void obtenerNombreLogs() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        String fecha = elegirFechaLog.getText();
        Date date = formatter.parse(fecha);

        SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy");
        String fechaTexto = formatter2.format(date);

        String fechaSplit[] = fechaTexto.split("/");
        String dia = fechaSplit[0];
        String mes = fechaSplit[1];
        String year = fechaSplit[2];
        nombreLog = year + mes + dia + "_LOGS";
        nombreError = year + mes + dia + "_ERROR";
    }

    /**
     * Lista los logs del directorio elegido que su nombre corresponden con la
     * fecha introducida.
     */
    private static File[] archivoLogs() {

        File[] files = directorio.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                boolean nombreArchivo = false;
                if ((name.contains(nombreLog))) {
                    nombreArchivo = true;
                } else {
                    nombreArchivo = false;
                }
                return nombreArchivo;
            }
        });
        return files;
    }

    /**
     * Lista los archivos _ERROR del directorio elegido que su nombre
     * corresponden con la fecha introducida.
     */
    private static File[] archivoError() {

        File[] files = directorio.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                boolean nombreArchivo = false;
                if ((name.contains(nombreError))) {
                    nombreArchivo = true;
                } else {
                    nombreArchivo = false;
                }
                return nombreArchivo;
            }
        });
        return files;
    }

    /**
     * Muestra los datos de cada operación recogidos en los logs.
     */
    private void analizarLog() throws IOException {
        File[] files = archivoLogs();
        boolean medioPago = true;
        String operacion = "";
        String clave = "";
        String finTransaccion = "";
        String importe = "";
        String medioDePago = "";
        String codMedioPago = "";

        for (int i = 0; i < files.length; i++) {

            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                if (linea.contains("INICIO DE TRANSACCION")) {
                    medioPago = false;
                    String datos[] = linea.split("\\(");
                    String datos1[] = datos[1].split("\\)");
                    //operacion = linea.substring(32, 36);
                    operacion = datos1[0];
                    clave = linea.substring(38, 43).replace("-", "");
                }
                if (linea.contains("<importeTotal>")) {
                    String datos[] = linea.split("</");
                    String datos1[] = datos[0].split(">");
                    importe = datos1[1];
                }
                if (linea.contains("<medioPago>") && !medioPago) {
                    String datos[] = linea.split("</");
                    String datos1[] = datos[0].split(">");
                    medioDePago = datos1[1];
                    medioPago = true;
                }
                if (linea.contains("<codMedioPago>")) {
                    String datos[] = linea.split("</");
                    String datos1[] = datos[0].split(">");
                    String codMedioPago1 = datos1[1];
                    codMedioPago = codMedioPago1.trim();
                }
                if (linea.contains("FINAL DE TRANSACCION")) {
                    String extraerFinTransaccion[] = linea.split("\\(");
                    String finalTransaccion = extraerFinTransaccion[2].replace("-", "");
                    finTransaccion = finalTransaccion.replace(")", "");
                    DefaultTableModel model = (DefaultTableModel) tablaDatos.getModel();
                    if (!medioDePago.equals("")) {
                        String datos[] = {operacion, clave, importe, medioDePago, finTransaccion};
                        model.addRow(datos);
                    } else {
                        String datos[] = {operacion, clave, importe, codMedioPago, finTransaccion};
                        model.addRow(datos);
                    }
                }

                linea = b.readLine();
            }
        }
    }

    /**
     * Muestra los diálogo mensajes de cada operación recogidos en los logs. El
     * usuario selecciona la operación.
     */
    private void verDialogoMensajes() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();
        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();
        int row = tablaDatos.getSelectedRow();
        if (tablaDatos.getRowSorter() != null) {
            row = tablaDatos.getRowSorter().convertRowIndexToModel(row);
        }
        String numOperacion = String.valueOf(tm.getValueAt(row, 0));

        for (int i = 0; i < files.length; i++) {

            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                if (linea.contains("INICIO DE TRANSACCION (" + numOperacion + ")")) {
                    while (linea != null && !linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                        if (linea.contains("mensajes:")) {
                            String datos[] = linea.split("mensajes:");
                            String error = datos[1];
                            DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                            String info[] = {error};
                            model.addRow(info);
                        }
                        if (linea.contains("observaciones: ERROR B.D:")) {
                            DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                            String datos0[] = linea.split("importe:");
                            String datos1[] = datos0[1].split("dato:");
                            String error1 = datos1[0].trim();

                            String datos2[] = linea.split("observaciones:");
                            String error2 = " - Importe: " + error1 + " " + datos2[1];
                            String info2[] = {error2};
                            model.addRow(info2);
                        }
                        linea = b.readLine();
                    }
                }
                linea = b.readLine();
            }
        }
    }

    /**
     * Muestra las trazas de la operación seleccionada por el usuario.
     */
    private void verOperacion() throws FileNotFoundException, IOException {
        try {
            File[] files = archivoLogs();
            TableModel tm = tablaDatos.getModel();
            int row = tablaDatos.getSelectedRow();
            if (tablaDatos.getRowSorter() != null) {
                row = tablaDatos.getRowSorter().convertRowIndexToModel(row);
            }
            String numOperacion = String.valueOf(tm.getValueAt(row, 0));

            for (int i = 0; i < files.length; i++) {

                FileReader f = new FileReader(files[i]);
                BufferedReader b = new BufferedReader(f);

                String linea = b.readLine();

                while (linea != null) {
                    if (linea.contains("INICIO DE TRANSACCION (" + numOperacion + ")")) {
                        while (linea != null && !linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                            DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                            model.addRow(new Object[]{linea});
                            if (!linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                                linea = b.readLine();
                            }
                        }
                    }
                    if (linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                        DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                        model.addRow(new Object[]{linea});
                    }
                    linea = b.readLine();
                }
                b.close();
            }
        } catch (Exception e) {
            verOperacionNoFinalizada();
        }
    }

    /**
     * Muestra las trazas de la operación seleccionada por el usuario, en el
     * caso de ser una operación no finalizada.
     */
    private void verOperacionNoFinalizada() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();
        TableModel tm = tablaDatos.getModel();
        int row = tablaDatos.getSelectedRow();
        if (tablaDatos.getRowSorter() != null) {
            row = tablaDatos.getRowSorter().convertRowIndexToModel(row);
        }
        String numOperacion = String.valueOf(tm.getValueAt(row, 0));

        for (int i = 0; i < files.length; i++) {

            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);

            String linea = b.readLine();

            while (linea != null) {
                if (linea.contains("INICIO DE TRANSACCION (" + numOperacion + ")")) {
                    while (linea != null && !linea.contains("INICIO DE TRANSACCION")) {
                        DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                        model.addRow(new Object[]{linea});
                        linea = b.readLine();
                    }
                }
                linea = b.readLine();
            }
            b.close();
        }
    }

    /**
     * Muestra los errores de los archivos _ERRORx del día introducido por el
     * usuario.
     */
    private void verErrorFile() throws IOException {
        File[] files = archivoError();

        for (int i = 0; i < files.length; i++) {

            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                if (linea.contains("Exception:")) {
                    DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                    model.addRow(new Object[]{linea});
                }
                linea = b.readLine();
            }
            b.close();
        }
    }

    /**
     * Muestra las fases de cada operación indicada por el usuario y sus
     * resultados.
     */
    private void verFases() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();
        int row = tablaDatos.getSelectedRow();
        if (tablaDatos.getRowSorter() != null) {
            row = tablaDatos.getRowSorter().convertRowIndexToModel(row);
        }
        String numOperacion = String.valueOf(tm.getValueAt(row, 0));

        for (int i = 0; i < files.length; i++) {

            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();

                if (linea.contains("INICIO DE TRANSACCION (" + numOperacion + ")")) {
                    while (linea != null && !linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                        if (linea.contains("*****FASE*****")) {
                            model.addRow(new Object[]{linea});
                        }
                        if (linea.contains("<RESULTADO:")) {
                            model.addRow(new Object[]{linea});
                        }
                        if (!linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                            linea = b.readLine();
                        }
                    }
                }
                linea = b.readLine();
            }
        }

    }

    /**
     * Muestra los GUIF y botones pulsados de la operación seleccionada por el
     * usuario.
     */
    private void verGUIF() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();
        int row = tablaDatos.getSelectedRow();
        if (tablaDatos.getRowSorter() != null) {
            row = tablaDatos.getRowSorter().convertRowIndexToModel(row);
        }
        String numOperacion = String.valueOf(tm.getValueAt(row, 0));

        for (int i = 0; i < files.length; i++) {

            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                if (linea.contains("INICIO DE TRANSACCION (" + numOperacion + ")")) {
                    while (linea != null && !linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                        if (linea.contains("*****GUIF*****")) {
                            DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                            model.addRow(new Object[]{linea});
                        }
                        if (linea.contains("[BOTON:")) {
                            DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                            model.addRow(new Object[]{linea});
                        }
                        if (!linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                            linea = b.readLine();
                        }
                    }
                }
                linea = b.readLine();
            }
        }

    }

    /**
     * Muestra todas las consultas realizadas a la BBDD en la operación
     * seleccionada por el usuario.
     */
    private void verConsultasSQL() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();
        int row = tablaDatos.getSelectedRow();
        if (tablaDatos.getRowSorter() != null) {
            row = tablaDatos.getRowSorter().convertRowIndexToModel(row);
        }
        String numOperacion = String.valueOf(tm.getValueAt(row, 0));

        for (int i = 0; i < files.length; i++) {

            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                if (linea.contains("INICIO DE TRANSACCION (" + numOperacion + ")")) {
                    while (linea != null && !linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                        if (linea.contains("*****SQL******")) {
                            DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                            model.addRow(new Object[]{linea});
                        }
                        if (!linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                            linea = b.readLine();
                        }
                    }
                }
                linea = b.readLine();
            }
        }
    }

    /**
     * Método para mostrar las trazas del arranque del terminal.
     */
    private void verArranque() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();

        FileReader f = new FileReader(files[0]);
        BufferedReader b = new BufferedReader(f);
        String linea = b.readLine();

        while (linea != null && (!linea.contains("*****FASE***** EmpleadoPuedeAbrirTerminalPh"))) {
            DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
            model.addRow(new Object[]{linea});
            if (!linea.contains("*****FASE***** EmpleadoPuedeAbrirTerminalPh")) {
                linea = b.readLine();
            }
        }
    }

    /**
     * Muestra los datos del terminal y su configuración.
     */
    private void verDatosTerminal() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();
        boolean escrito = false;

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();

        for (int i = 0; i < files.length; i++) {
            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                if (!escrito) {
                    if (linea.contains("<terminal>")) {
                        String datos[] = linea.split("</");
                        String datos1[] = datos[0].split(">");
                        model.addRow(new Object[]{"Terminal: " + datos1[1]});
                    }
                    if (linea.contains("<empresa>")) {
                        String datos[] = linea.split("</");
                        String datos1[] = datos[0].split(">");
                        model.addRow(new Object[]{"Empresa: " + datos1[1]});
                    }
                    if (linea.contains("<centro>")) {
                        String datos[] = linea.split("</");
                        String datos1[] = datos[0].split(">");
                        model.addRow(new Object[]{"Centro: " + datos1[1]});
                        escrito = true;
                    }
                }
                linea = b.readLine();
            }
        }
    }

    /**
     * Método para conectarse al terminal en remoto.
     */
    private void realizaConexion() {
        try {
            
            String password = new String(passwordText.getPassword());
            Process p = Runtime.getRuntime().exec("net use z: \\\\" + ipText.getText() + "\\c$ /USER:"+userText.getText()+" "+password);
            InputStream in = p.getInputStream();

            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);

            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.contains("Se ha completado el comando correctamente")) {
                    conexionRealizada = true;
                }
            }
            br.close();

        } catch (IOException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            infoConexion.setText("No es posible Conectar");
        }
    }

    /**
     * Método para abrir y visualizar archivos del terminal introducido.
     */
    private void verArchivo() throws FileNotFoundException, IOException {
        File file = exploradorTerminal.getSelectedFile();
        FileReader f = new FileReader(file);
        BufferedReader b = new BufferedReader(f);
        String linea = b.readLine();
        while (linea != null) {
            DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
            model.addRow(new Object[]{linea});
            linea = b.readLine();
        }
    }

    /**
     * Método para mostrar el xml de cada operación finalizada.
     */
    private void verXmlOperacion() throws IOException {
        File[] files = archivoLogs();

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();
        int row = tablaDatos.getSelectedRow();
        if (tablaDatos.getRowSorter() != null) {
            row = tablaDatos.getRowSorter().convertRowIndexToModel(row);
        }
        String numOperacion = String.valueOf(tm.getValueAt(row, 0));

        for (int i = 0; i < files.length; i++) {
            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();

                if (linea.contains("INICIO DE TRANSACCION (" + numOperacion + ")")) {
                    while (linea != null && !linea.contains("FINAL DE TRANSACCION (" + numOperacion + ")")) {
                        if (linea.contains("<datosOperacion>")) {
                            while (!linea.contains("</datosOperacion>")) {
                                model.addRow(new Object[]{linea});
                                linea = b.readLine();
                            }
                            if (linea.contains("</datosOperacion>")) {
                                model.addRow(new Object[]{linea});
                            }
                        }
                        linea = b.readLine();
                    }
                }
                linea = b.readLine();
            }
        }
    }

    /**
     * Muestra las versiones de los componentes de la aplicación.
     */
    private void verVersiones() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();

        for (int i = 0; i < files.length; i++) {

            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();

                if (linea.contains("->Deploying jar file .\\..\\pds2\\lib\\dataServices.jar version:")) {
                    String[] datos = linea.split("version:");
                    versionData.setText(datos[1]);
                }
                if (linea.contains("->Deploying jar file .\\..\\pds2\\lib\\devicesServices.jar version:")) {
                    String[] datos = linea.split("version:");
                    versionDevice.setText(datos[1]);
                }
                if (linea.contains("DEL TERMINAL ES:")) {
                    String[] datos = linea.split("ES:");
                    versionApp.setText(datos[1]);
                }
                if (linea.contains("")) {
                    //Conexflow (Hay que extraerlo de cfpv.tra)
                    versionCf.setText("----");
                }
                if (linea.contains("")) {
                    //FW Ingénico (Hay que extraerlo de cfpv.tra)
                    versionFw.setText("----");
                }

                linea = b.readLine();
            }
        }

    }

    /**
     * Método para cerrar la conexión con el terminal.
     */
    private void cerrarConexionAnterior() {
        try {
            Runtime.getRuntime().exec("net use z: /delete");
        } catch (IOException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Método para descargar el contenido que se muestra por pantalla, en un
     * archivo.
     */
    private void descargarArchivo() throws FileNotFoundException, IOException {
        File archivo = null;
        String nombreArchivo = exploradorTerminal.getSelectedFile().getName();
        archivo = new File("C:/Incigest/Descargas" + nombreArchivo);

        BufferedWriter bfw = new BufferedWriter(new FileWriter(archivo));

        for (int i = 0; i < tablaInfo.getRowCount(); i++) {
            for (int j = 0; j < tablaInfo.getColumnCount(); j++) {
                bfw.write((String) (tablaInfo.getValueAt(i, j)));
                if (j < tablaInfo.getColumnCount() - 1) {
                    bfw.write(",");
                }
            }
            bfw.newLine();
        }
        bfw.close();
    }

    /**
     * Método para seleccionar el filtro de la tabla de datos relevantes de las
     * operaciones.
     */
    private void filtrarTabla(int seleccion) {
        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tm);
        switch (seleccion) {
            case 0:
                botonBuscar.setEnabled(true);
                tablaDatos.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("", 3));
                break;
            case 1:
                botonBuscar.setEnabled(false);
                tablaDatos.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("EFECTIVO", 3));
                break;
            case 2:
                botonBuscar.setEnabled(false);
                tablaDatos.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("TARJETA BANCARIA", 3));
                break;
        }
    }

    /**
     * Método para ver más datos del arranque y su estado correcto o con error.
     */
    private void verDatosArranque() throws FileNotFoundException, IOException {
        File[] files = archivoLogs();

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();

        for (int i = 0; i < files.length; i++) {
            FileReader f = new FileReader(files[i]);
            BufferedReader b = new BufferedReader(f);
            String linea = b.readLine();

            while (linea != null) {
                DefaultTableModel model = (DefaultTableModel) tablaInfo.getModel();
                if (linea.contains("*****FASE***** ActualizarTablasMaestrasPh")) {
                    while (linea != null && !linea.contains("<RESULTADO:")) {
                        linea = b.readLine();
                    }
                    if (linea.contains("<RESULTADO: OK")) {
                        estadoMaestras.setForeground(Color.green);
                        estadoMaestras.setText("Correcto");
                    }
                    if (linea.contains("<RESULTADO: NOK")) {
                        estadoMaestras.setForeground(Color.red);
                        estadoMaestras.setText("Error");
                    }
                }

                if (linea.contains("*****FASE***** EstablecerEmpleadoSesionPh")) {
                    while (linea != null && !linea.contains("<RESULTADO:")) {
                        linea = b.readLine();
                    }
                    if (linea.contains("<RESULTADO: OK - END") || linea.contains("<RESULTADO: OK")) {
                        estadoEmpleado.setForeground(Color.green);
                        estadoEmpleado.setText("Correcto");
                    }
                    if (linea.contains("<RESULTADO: NOK") || linea.contains("<RESULTADO: NOK - END")) {
                        estadoEmpleado.setForeground(Color.red);
                        estadoEmpleado.setText("Error");
                    }
                }

                if (linea.contains("*****FASE***** AbrirTerminalPh")) {
                    while (linea != null && !linea.contains("<RESULTADO:")) {
                        linea = b.readLine();
                    }
                    if (linea.contains("<RESULTADO: OK")) {
                        estadoAbrir.setForeground(Color.green);
                        estadoAbrir.setText("Correcto");
                    }
                    if (linea.contains("<RESULTADO: NOK")) {
                        estadoAbrir.setForeground(Color.red);
                        estadoAbrir.setText("Error");
                    }
                }

                linea = b.readLine();
            }
        }
    }

    /**
     * Limpia la tabla de la información mostrada de cada opción del menú.
     */
    private void limpiarTexto() {
        DefaultTableModel tb = (DefaultTableModel) tablaInfo.getModel();
        int a = tablaInfo.getRowCount() - 1;
        for (int i = a; i >= 0; i--) {
            tb.removeRow(tb.getRowCount() - 1);
        }
    }

    /**
     * Limpia la tabla de los datos relevantes de las operaciones.
     */
    private void limpiarTabla() {
        DefaultTableModel tb = (DefaultTableModel) tablaDatos.getModel();
        int a = tablaDatos.getRowCount() - 1;
        for (int i = a; i >= 0; i--) {
            tb.removeRow(tb.getRowCount() - 1);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ventanaExplorador = new javax.swing.JDialog();
        elegirDirectorio = new javax.swing.JFileChooser();
        botonAbrirDirectorio = new javax.swing.JButton();
        ventanaAnalisis = new javax.swing.JDialog();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaDatos = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        seleccionaBox = new javax.swing.JComboBox<>();
        botonVer = new javax.swing.JButton();
        textoBusqueda = new javax.swing.JTextField();
        botonBuscar = new javax.swing.JButton();
        filtro = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        ventanaDatos = new javax.swing.JDialog();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaInfo = new javax.swing.JTable();
        textoBusquedaLog = new javax.swing.JTextField();
        botonBuscarEnLog = new javax.swing.JButton();
        botonSiguiente = new javax.swing.JButton();
        botonDescarga = new javax.swing.JButton();
        botonVerDatosArranque = new javax.swing.JButton();
        dialogoAviso = new javax.swing.JDialog();
        cerrarDialogo = new javax.swing.JButton();
        infoAvisoLabel = new javax.swing.JLabel();
        ventanaVerMas = new javax.swing.JDialog();
        jPanel2 = new javax.swing.JPanel();
        ipText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        botonAceptarMas = new javax.swing.JButton();
        funcionElegida = new javax.swing.JComboBox<>();
        infoConexion = new java.awt.Label();
        ventanaExploradorTerminal = new javax.swing.JDialog();
        exploradorTerminal = new javax.swing.JFileChooser();
        jButton1 = new javax.swing.JButton();
        VentanaMasInfo = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        estadoMaestras = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        estadoEmpleado = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        estadoAbrir = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        botonesPais = new javax.swing.ButtonGroup();
        ventanaVersiones = new javax.swing.JDialog();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        versionDevice = new javax.swing.JLabel();
        versionData = new javax.swing.JLabel();
        versionApp = new javax.swing.JLabel();
        versionCf = new javax.swing.JLabel();
        versionFw = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        inicioSesionTerminal = new javax.swing.JDialog();
        userText = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        labelLogoUser = new javax.swing.JLabel();
        botonAcceso = new javax.swing.JButton();
        labelInfoUser = new javax.swing.JLabel();
        passwordText = new javax.swing.JPasswordField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        botonSeleccionarFichero = new javax.swing.JButton();
        botonAnalizar = new javax.swing.JButton();
        elegirFechaLog = new datechooser.beans.DateChooserCombo();
        infoLabel = new javax.swing.JLabel();

        ventanaExplorador.setResizable(false);
        ventanaExplorador.setSize(new java.awt.Dimension(700, 600));

        elegirDirectorio.setControlButtonsAreShown(false);
        elegirDirectorio.setCurrentDirectory(new java.io.File("C:\\"));
            elegirDirectorio.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

            botonAbrirDirectorio.setText("Abrir");
            botonAbrirDirectorio.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonAbrirDirectorioActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout ventanaExploradorLayout = new javax.swing.GroupLayout(ventanaExplorador.getContentPane());
            ventanaExplorador.getContentPane().setLayout(ventanaExploradorLayout);
            ventanaExploradorLayout.setHorizontalGroup(
                ventanaExploradorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ventanaExploradorLayout.createSequentialGroup()
                    .addGap(75, 75, 75)
                    .addGroup(ventanaExploradorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(elegirDirectorio, javax.swing.GroupLayout.PREFERRED_SIZE, 522, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ventanaExploradorLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 390, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(botonAbrirDirectorio, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(22, 22, 22)))
                    .addContainerGap(88, Short.MAX_VALUE))
            );
            ventanaExploradorLayout.setVerticalGroup(
                ventanaExploradorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ventanaExploradorLayout.createSequentialGroup()
                    .addGap(50, 50, 50)
                    .addComponent(elegirDirectorio, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(botonAbrirDirectorio, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(62, Short.MAX_VALUE))
            );

            ventanaAnalisis.setSize(new java.awt.Dimension(1200, 700));

            tablaDatos.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                    "Operación", "Clave", "Importe", "Medio de Pago", "Finalización"
                }
            ));
            tablaDatos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            jScrollPane1.setViewportView(tablaDatos);

            jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
            jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel4.setText("Seleccione una operación");

            seleccionaBox.setBackground(new java.awt.Color(153, 204, 255));
            seleccionaBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Operación", "Diálogo Mensajes", "Fases", "GUIF", "Consultas BBDD", "Errores", "Datos Terminal", "Arranque", "XML Operación", "Versiones", "Acceso al Terminal" }));

            botonVer.setBackground(new java.awt.Color(153, 204, 255));
            botonVer.setText("Ver");
            botonVer.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonVerActionPerformed(evt);
                }
            });

            textoBusqueda.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    textoBusquedaKeyPressed(evt);
                }
            });

            botonBuscar.setText("Buscar");
            botonBuscar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonBuscarActionPerformed(evt);
                }
            });

            filtro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todo", "Efectivo", "Tarjeta" }));
            filtro.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    filtroActionPerformed(evt);
                }
            });

            jLabel3.setText("Filtro:");

            javax.swing.GroupLayout ventanaAnalisisLayout = new javax.swing.GroupLayout(ventanaAnalisis.getContentPane());
            ventanaAnalisis.getContentPane().setLayout(ventanaAnalisisLayout);
            ventanaAnalisisLayout.setHorizontalGroup(
                ventanaAnalisisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ventanaAnalisisLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(ventanaAnalisisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ventanaAnalisisLayout.createSequentialGroup()
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(textoBusqueda, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(botonBuscar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(18, 18, Short.MAX_VALUE)
                            .addComponent(jLabel3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(filtro, 0, 100, Short.MAX_VALUE)
                            .addGap(26, 26, 26)
                            .addComponent(seleccionaBox, 0, 321, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(botonVer, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
                        .addComponent(jScrollPane1))
                    .addContainerGap())
            );
            ventanaAnalisisLayout.setVerticalGroup(
                ventanaAnalisisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ventanaAnalisisLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(ventanaAnalisisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(seleccionaBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(botonVer)
                        .addComponent(textoBusqueda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(botonBuscar)
                        .addGroup(ventanaAnalisisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(filtro, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addComponent(jLabel4))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                    .addContainerGap())
            );

            tablaInfo.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                    {null}
                },
                new String [] {
                    ""
                }
            ));
            tablaInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            tablaInfo.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            jScrollPane2.setViewportView(tablaInfo);

            textoBusquedaLog.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    textoBusquedaLogKeyPressed(evt);
                }
            });

            botonBuscarEnLog.setText("Buscar");
            botonBuscarEnLog.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonBuscarEnLogActionPerformed(evt);
                }
            });

            botonSiguiente.setText("Siguiente");
            botonSiguiente.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonSiguienteActionPerformed(evt);
                }
            });

            botonDescarga.setText("Descargar");
            botonDescarga.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonDescargaActionPerformed(evt);
                }
            });

            botonVerDatosArranque.setText("Ver Más");
            botonVerDatosArranque.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonVerDatosArranqueActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout ventanaDatosLayout = new javax.swing.GroupLayout(ventanaDatos.getContentPane());
            ventanaDatos.getContentPane().setLayout(ventanaDatosLayout);
            ventanaDatosLayout.setHorizontalGroup(
                ventanaDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(ventanaDatosLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(textoBusquedaLog, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(botonBuscarEnLog)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(botonSiguiente)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
                    .addComponent(botonVerDatosArranque)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(botonDescarga)
                    .addContainerGap())
            );
            ventanaDatosLayout.setVerticalGroup(
                ventanaDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ventanaDatosLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(ventanaDatosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(textoBusquedaLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(botonBuscarEnLog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(botonSiguiente)
                        .addComponent(botonDescarga)
                        .addComponent(botonVerDatosArranque))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                    .addContainerGap())
            );

            dialogoAviso.setUndecorated(true);
            dialogoAviso.setResizable(false);

            cerrarDialogo.setText("Aceptar");
            cerrarDialogo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cerrarDialogoActionPerformed(evt);
                }
            });

            infoAvisoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            javax.swing.GroupLayout dialogoAvisoLayout = new javax.swing.GroupLayout(dialogoAviso.getContentPane());
            dialogoAviso.getContentPane().setLayout(dialogoAvisoLayout);
            dialogoAvisoLayout.setHorizontalGroup(
                dialogoAvisoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(infoAvisoLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dialogoAvisoLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cerrarDialogo)
                    .addGap(183, 183, 183))
            );
            dialogoAvisoLayout.setVerticalGroup(
                dialogoAvisoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dialogoAvisoLayout.createSequentialGroup()
                    .addContainerGap(17, Short.MAX_VALUE)
                    .addComponent(infoAvisoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cerrarDialogo)
                    .addGap(12, 12, 12))
            );

            jLabel5.setText("Función:");

            jLabel6.setText("Dirección IP:");

            botonAceptarMas.setText("Aceptar");
            botonAceptarMas.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonAceptarMasActionPerformed(evt);
                }
            });

            funcionElegida.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ver Archivos" }));

            infoConexion.setAlignment(java.awt.Label.CENTER);
            infoConexion.setForeground(new java.awt.Color(0, 153, 255));

            javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
            jPanel2.setLayout(jPanel2Layout);
            jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(122, 122, 122)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(funcionElegida, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ipText, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(infoConexion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(botonAceptarMas, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(138, Short.MAX_VALUE))
            );
            jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(155, 155, 155)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(funcionElegida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(28, 28, 28)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ipText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(botonAceptarMas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(infoConexion, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(199, 199, 199))
            );

            javax.swing.GroupLayout ventanaVerMasLayout = new javax.swing.GroupLayout(ventanaVerMas.getContentPane());
            ventanaVerMas.getContentPane().setLayout(ventanaVerMasLayout);
            ventanaVerMasLayout.setHorizontalGroup(
                ventanaVerMasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );
            ventanaVerMasLayout.setVerticalGroup(
                ventanaVerMasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            );

            exploradorTerminal.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
            exploradorTerminal.setControlButtonsAreShown(false);
            exploradorTerminal.setCurrentDirectory(new java.io.File("Z:\\sfctrl"));

            jButton1.setText("Abrir");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout ventanaExploradorTerminalLayout = new javax.swing.GroupLayout(ventanaExploradorTerminal.getContentPane());
            ventanaExploradorTerminal.getContentPane().setLayout(ventanaExploradorTerminalLayout);
            ventanaExploradorTerminalLayout.setHorizontalGroup(
                ventanaExploradorTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(exploradorTerminal, javax.swing.GroupLayout.DEFAULT_SIZE, 848, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ventanaExploradorTerminalLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(19, 19, 19))
            );
            ventanaExploradorTerminalLayout.setVerticalGroup(
                ventanaExploradorTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ventanaExploradorTerminalLayout.createSequentialGroup()
                    .addComponent(exploradorTerminal, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                    .addGap(18, 18, 18))
            );

            jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
            jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel7.setText("ESTADO DEL ARRANQUE");

            jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
            jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel8.setText("Descarga de Maestras: ");

            estadoMaestras.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
            estadoMaestras.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
            jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel10.setText("Sesión Empleado: ");

            estadoEmpleado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
            estadoEmpleado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
            jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel11.setText("Abrir Terminal: ");

            estadoAbrir.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
            estadoAbrir.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            jButton2.setText("Aceptar");
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton2ActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(estadoMaestras, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(estadoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(estadoAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(86, 86, 86)
                                    .addComponent(jLabel7)))
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
            );
            jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel7)
                    .addGap(36, 36, 36)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8)
                        .addComponent(estadoMaestras, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(estadoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel11)
                        .addComponent(estadoAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(48, 48, 48)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(26, Short.MAX_VALUE))
            );

            javax.swing.GroupLayout VentanaMasInfoLayout = new javax.swing.GroupLayout(VentanaMasInfo.getContentPane());
            VentanaMasInfo.getContentPane().setLayout(VentanaMasInfoLayout);
            VentanaMasInfoLayout.setHorizontalGroup(
                VentanaMasInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(VentanaMasInfoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
            );
            VentanaMasInfoLayout.setVerticalGroup(
                VentanaMasInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(VentanaMasInfoLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
            );

            jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
            jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel9.setText("Versiones");

            jLabel12.setText("Layer deviceService.jar: ");

            jLabel13.setText("Layer dataService.jar: ");

            jLabel14.setText("Aplicación: ");

            jLabel15.setText("Conexflow: ");

            jLabel16.setText("FW Ingénico: ");

            jButton3.setText("Aceptar");
            jButton3.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton3ActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout ventanaVersionesLayout = new javax.swing.GroupLayout(ventanaVersiones.getContentPane());
            ventanaVersiones.getContentPane().setLayout(ventanaVersionesLayout);
            ventanaVersionesLayout.setHorizontalGroup(
                ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ventanaVersionesLayout.createSequentialGroup()
                    .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(ventanaVersionesLayout.createSequentialGroup()
                            .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(ventanaVersionesLayout.createSequentialGroup()
                                    .addGap(136, 136, 136)
                                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(ventanaVersionesLayout.createSequentialGroup()
                                    .addGap(77, 77, 77)
                                    .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel12)
                                        .addComponent(jLabel14)
                                        .addComponent(jLabel15)
                                        .addComponent(jLabel16)
                                        .addComponent(jLabel13))
                                    .addGap(48, 48, 48)
                                    .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(versionFw, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(versionCf, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(versionApp, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(versionData, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(versionDevice, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGap(0, 79, Short.MAX_VALUE))
                        .addGroup(ventanaVersionesLayout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addContainerGap())
            );
            ventanaVersionesLayout.setVerticalGroup(
                ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ventanaVersionesLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(55, 55, 55)
                    .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel12)
                        .addComponent(versionDevice, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(versionData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(18, 18, 18)
                    .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel14)
                        .addComponent(versionApp, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(versionCf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGap(18, 18, 18)
                    .addGroup(ventanaVersionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(versionFw, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(32, 32, 32))
            );

            jLabel17.setText("Usuario: ");

            jLabel18.setText("Contraseña: ");

            labelLogoUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/analizalog/logoUser.png"))); // NOI18N

            botonAcceso.setText("Acceder");
            botonAcceso.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonAccesoActionPerformed(evt);
                }
            });

            labelInfoUser.setForeground(new java.awt.Color(255, 51, 51));
            labelInfoUser.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            labelInfoUser.setText("Introduzca usuario y/o contraseña");

            javax.swing.GroupLayout inicioSesionTerminalLayout = new javax.swing.GroupLayout(inicioSesionTerminal.getContentPane());
            inicioSesionTerminal.getContentPane().setLayout(inicioSesionTerminalLayout);
            inicioSesionTerminalLayout.setHorizontalGroup(
                inicioSesionTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(inicioSesionTerminalLayout.createSequentialGroup()
                    .addGap(199, 199, 199)
                    .addGroup(inicioSesionTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(inicioSesionTerminalLayout.createSequentialGroup()
                            .addGap(99, 99, 99)
                            .addComponent(botonAcceso, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(inicioSesionTerminalLayout.createSequentialGroup()
                            .addGroup(inicioSesionTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addGap(18, 18, 18)
                            .addGroup(inicioSesionTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(userText)
                                .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGroup(inicioSesionTerminalLayout.createSequentialGroup()
                    .addComponent(labelInfoUser, javax.swing.GroupLayout.PREFERRED_SIZE, 692, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inicioSesionTerminalLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelLogoUser, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(288, 288, 288))
            );
            inicioSesionTerminalLayout.setVerticalGroup(
                inicioSesionTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(inicioSesionTerminalLayout.createSequentialGroup()
                    .addGap(78, 78, 78)
                    .addComponent(labelLogoUser, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(28, 28, 28)
                    .addGroup(inicioSesionTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(userText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel17))
                    .addGap(28, 28, 28)
                    .addGroup(inicioSesionTerminalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18)
                        .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(42, 42, 42)
                    .addComponent(botonAcceso, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(38, 38, 38)
                    .addComponent(labelInfoUser)
                    .addContainerGap(118, Short.MAX_VALUE))
            );

            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            setResizable(false);
            addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    formWindowClosing(evt);
                }
            });

            jLabel1.setText("Selecciona la fecha:");

            jLabel2.setText("Selecciona un directorio:");

            botonSeleccionarFichero.setText("Seleccionar");
            botonSeleccionarFichero.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonSeleccionarFicheroActionPerformed(evt);
                }
            });

            botonAnalizar.setText("Analizar");
            botonAnalizar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    botonAnalizarActionPerformed(evt);
                }
            });

            elegirFechaLog.setCurrentView(new datechooser.view.appearance.AppearancesList("Light",
                new datechooser.view.appearance.ViewAppearance("custom",
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 0),
                        new java.awt.Color(0, 0, 255),
                        false,
                        true,
                        new datechooser.view.appearance.swing.ButtonPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 0),
                        new java.awt.Color(0, 0, 255),
                        true,
                        true,
                        new datechooser.view.appearance.swing.ButtonPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 255),
                        new java.awt.Color(0, 0, 255),
                        false,
                        true,
                        new datechooser.view.appearance.swing.ButtonPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(128, 128, 128),
                        new java.awt.Color(0, 0, 255),
                        false,
                        true,
                        new datechooser.view.appearance.swing.LabelPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 0),
                        new java.awt.Color(0, 0, 255),
                        false,
                        true,
                        new datechooser.view.appearance.swing.LabelPainter()),
                    new datechooser.view.appearance.swing.SwingCellAppearance(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11),
                        new java.awt.Color(0, 0, 0),
                        new java.awt.Color(255, 0, 0),
                        false,
                        false,
                        new datechooser.view.appearance.swing.ButtonPainter()),
                    (datechooser.view.BackRenderer)null,
                    false,
                    true)));
        elegirFechaLog.setCalendarPreferredSize(new java.awt.Dimension(260, 225));
        elegirFechaLog.setNothingAllowed(false);
        elegirFechaLog.setWeekStyle(datechooser.view.WeekDaysStyle.SHORT);
        elegirFechaLog.setFieldFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        elegirFechaLog.setNavigateFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
        elegirFechaLog.setCurrentNavigateIndex(0);
        elegirFechaLog.setBehavior(datechooser.model.multiple.MultyModelBehavior.SELECT_SINGLE);
        elegirFechaLog.setShowOneMonth(true);

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(botonAnalizar, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(elegirFechaLog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(botonSeleccionarFichero, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(164, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(elegirFechaLog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(botonSeleccionarFichero))
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(botonAnalizar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(153, 153, 153))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Abre el explorador y selecciona un directorio.
     */
    private void botonSeleccionarFicheroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSeleccionarFicheroActionPerformed
        ventanaExplorador.setLocationRelativeTo(null);
        ventanaExplorador.setVisible(true);
    }//GEN-LAST:event_botonSeleccionarFicheroActionPerformed

    /**
     * Cierra el explorador y guarda el directorio elegido.
     */
    private void botonAbrirDirectorioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAbrirDirectorioActionPerformed
        directorio = elegirDirectorio.getSelectedFile();
        botonSeleccionarFichero.setText(directorio.getName());
        ventanaExplorador.setVisible(false);
    }//GEN-LAST:event_botonAbrirDirectorioActionPerformed

    /**
     * Botón para mostrar el análisis del log.
     */
    private void botonAnalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAnalizarActionPerformed
        limpiarTabla();
        textoBusqueda.setText("");
        if (directorio != null && elegirFechaLog.getText() != "") {
            try {
                obtenerNombreLogs();
            } catch (ParseException ex) {
                Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                analizarLog();
            } catch (IOException ex) {
                Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                infoLabel.setText("No se encuentran logs en esa fecha/directorio");
            }
            ventanaAnalisis.setLocationRelativeTo(null);
            ventanaAnalisis.setVisible(true);
        } else {
            infoLabel.setText("Introduzca el directorio y/o fecha de los logs");
        }
    }//GEN-LAST:event_botonAnalizarActionPerformed

    /**
     * Botón para mostrar datos de la operación según la selección del usuario
     * desde el menu.
     */
    private void botonVerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonVerActionPerformed
        botonDescarga.setVisible(false);
        botonVerDatosArranque.setVisible(false);
        tablaInfo.setDefaultRenderer(Object.class, new RenderCeldaDefault());
        int seleccion = seleccionaBox.getSelectedIndex();
        if ((!tablaDatos.getSelectionModel().isSelectionEmpty()) || seleccion == 5 || seleccion == 6 || seleccion == 7 || seleccion == 9 || seleccion == 10) {
            limpiarTexto();
            filaBusqueda = 0;
            busquedas.removeAll(busquedas);
            textoBusquedaLog.setText("");
            ventanaDatos.setBounds(ventanaAnalisis.getBounds());
            botonSiguiente.setEnabled(false);
            switch (seleccion) {
                case 0:
                try {
                    verOperacion();
                    ventanaDatos.setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
                case 1:
                try {
                    verDialogoMensajes();
                    ventanaDatos.setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
                case 2:
                try {
                    tablaInfo.setDefaultRenderer(Object.class, new RenderCelda());
                    verFases();
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                ventanaDatos.setVisible(true);
                break;

                case 3:
                try {
                    verGUIF();
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                ventanaDatos.setVisible(true);
                break;

                case 4:
                try {
                    verConsultasSQL();
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                ventanaDatos.setVisible(true);
                break;

                case 5:
                    try {
                    verErrorFile();
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                ventanaDatos.setVisible(true);
                break;

                case 6:
                    try {
                    verDatosTerminal();
                    ventanaDatos.setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

                case 7:
                    try {
                    botonVerDatosArranque.setVisible(true);
                    verArranque();
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                ventanaDatos.setVisible(true);
                break;

                case 8:
                    try {
                    verXmlOperacion();
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                ventanaDatos.setVisible(true);
                break;

                case 9:
                    try {
                    verVersiones();
                } catch (IOException ex) {
                    Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
                ventanaVersiones.setSize(470, 390);
                ventanaVersiones.setLocationRelativeTo(null);
                ventanaVersiones.setVisible(true);
                break;

                case 10:
                    infoConexion.setText("");
                    labelInfoUser.setVisible(false);
                    
                    inicioSesionTerminal.setBounds(ventanaExplorador.getBounds());
                    inicioSesionTerminal.setVisible(true);
                    break;
            }
        } else {
            infoAvisoLabel.setText("Seleccione una operación de la tabla para ver los datos.");
            dialogoAviso.setSize(462, 96);
            dialogoAviso.setLocationRelativeTo(null);
            dialogoAviso.setVisible(true);
        }
    }//GEN-LAST:event_botonVerActionPerformed

    /**
     * Botón para cerrar el aviso de error.
     */
    private void cerrarDialogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cerrarDialogoActionPerformed
        dialogoAviso.setVisible(false);
    }//GEN-LAST:event_cerrarDialogoActionPerformed

    /**
     * El botón de buscar también se acciona con la tecla enter, en el campo de
     * texto.
     */
    private void textoBusquedaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textoBusquedaKeyPressed
        if (evt.getExtendedKeyCode() == VK_ENTER)
            botonBuscar.doClick();
    }//GEN-LAST:event_textoBusquedaKeyPressed

    /**
     * Botón para realizar la búsqueda de numero de transacción.
     */
    private void botonBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarActionPerformed
        String numOperacion = textoBusqueda.getText();

        DefaultTableModel tm = (DefaultTableModel) tablaDatos.getModel();
        for (int i = 0; i < tm.getRowCount(); i++) {
            if (tm.getValueAt(i, 0).equals(numOperacion)) {
                //tablaDatos.changeSelection(i, 0, false, false);
                tablaDatos.addRowSelectionInterval(i, i);
                break;
            }
        }
    }//GEN-LAST:event_botonBuscarActionPerformed

    /**
     * Filtro de operaciones que se muestran en la tabla de datos relevantes.
     */
    private void filtroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filtroActionPerformed
        int seleccion = filtro.getSelectedIndex();
        filtrarTabla(seleccion);
    }//GEN-LAST:event_filtroActionPerformed

    /**
     * Botón para visualizar un archivo del terminal conectado.
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            verArchivo();
            ventanaDatos.setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Método para cerrar la conexión con el terminal al salir de la aplicación.
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cerrarConexionAnterior();
    }//GEN-LAST:event_formWindowClosing

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        VentanaMasInfo.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * Método para elegir la opción del menú de conexión al terminal.
     */
    private void botonAceptarMasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAceptarMasActionPerformed
     
        int seleccion = funcionElegida.getSelectedIndex();

        switch (seleccion) {

            case 0: //Ver archivos del terminal
                botonDescarga.setVisible(true);
                cerrarConexionAnterior();
                Instant start = Instant.now(); //Comienza tiempo del proceso de conexión
                infoConexion.setText("Conectando... Espere, por favor");
                realizaConexion();

                while (!conexionRealizada && ventanaVerMas.isVisible()) {
                    infoConexion.setText("Conectando...");
                    Duration duration = Duration.between(start, Instant.now());
                    if (duration.toMillis() > 2000) {
                        infoAvisoLabel.setText("Demasiado tiempo en conectar, es posible que el terminal esté offline");
                        dialogoAviso.setVisible(true);
                        dialogoAviso.setSize(462, 96);
                        dialogoAviso.setLocationRelativeTo(null);
                        ventanaVerMas.setVisible(false);
                    }
                }
                if (conexionRealizada) {
                    infoConexion.setText("Terminal Conectado. Abriendo...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    File directorioRemoto = new File("Z:/sfctrl");
                    exploradorTerminal.setSelectedFile(directorioRemoto);
                    ventanaExploradorTerminal.setBounds(ventanaVerMas.getBounds());
                    ventanaExploradorTerminal.setVisible(true);
                }
                break;
        }
    }//GEN-LAST:event_botonAceptarMasActionPerformed

    /**
     * Método para cerrar ventana de versiones
     */
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        ventanaVersiones.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void botonAccesoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAccesoActionPerformed
        if (!userText.getText().equals("")){
            ventanaVerMas.setBounds(ventanaExplorador.getBounds());
            ventanaVerMas.setVisible(true);
        }else{
            labelInfoUser.setVisible(true);
        }
    }//GEN-LAST:event_botonAccesoActionPerformed

    private void botonVerDatosArranqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonVerDatosArranqueActionPerformed
        VentanaMasInfo.setSize(415, 325);
        VentanaMasInfo.setVisible(true);
        VentanaMasInfo.setLocationRelativeTo(null);
        try {
            verDatosArranque();
        } catch (IOException ex) {
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_botonVerDatosArranqueActionPerformed

    /**
     * Botón para descargar un archivo visualizado del terminal conectado.
     */
    private void botonDescargaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonDescargaActionPerformed
        try {
            descargarArchivo();
            dialogoAviso.setSize(462, 96);
            dialogoAviso.setLocationRelativeTo(null);
            infoAvisoLabel.setText("Se ha descargado el archivo en el directorio de la aplicacion");
            dialogoAviso.setVisible(true);
        } catch (IOException ex) {
            dialogoAviso.setSize(462, 96);
            dialogoAviso.setLocationRelativeTo(null);
            infoAvisoLabel.setText("Error al descargar");
            dialogoAviso.setVisible(true);
            Logger.getLogger(VentanaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_botonDescargaActionPerformed

    /**
     * Este botón recoge la búsqueda realizada y muestra seleccionadas cada una
     * de las filas que correspondan a la búsqueda, por orden, según pulsas al
     * botón.
     */
    private void botonSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSiguienteActionPerformed
        tablaInfo.changeSelection(busquedas.get(filaBusqueda), 0, false, false);
        filaBusqueda++;
        if ((busquedas.size()) == filaBusqueda) {
            filaBusqueda = 0;
        }
    }//GEN-LAST:event_botonSiguienteActionPerformed

    /**
     * Botón para realizar una búsqueda dentro de una operación. Guarda en una
     * ArrayList todas las filas con la información buscada.
     */
    private void botonBuscarEnLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarEnLogActionPerformed
        boolean resultadoEncontrado = false;

        filaBusqueda = 0;
        busquedas.removeAll(busquedas);
        lastElement = 0;
        String busqueda = textoBusquedaLog.getText().toLowerCase();
        DefaultTableModel tm = (DefaultTableModel) tablaInfo.getModel();

        for (int i = lastElement; i < tm.getRowCount(); i++) {
            String resultado = tm.getValueAt(i, 0).toString().toLowerCase();
            if (resultado.contains(busqueda)) {
                resultadoEncontrado = true;
                busquedas.add((int) i);
                if (!busquedas.isEmpty()) {
                    int lastIdx = busquedas.size() - 1;
                    lastElement = busquedas.get(lastIdx);
                }
            }
        }

        if (!busquedas.isEmpty()) {
            botonSiguiente.setEnabled(true);
        }
        if (!resultadoEncontrado) {
            botonSiguiente.setEnabled(false);
            dialogoAviso.setSize(462, 96);
            dialogoAviso.setLocationRelativeTo(null);
            infoAvisoLabel.setText("Búsqueda sin resultados");
            dialogoAviso.setVisible(true);
        }
    }//GEN-LAST:event_botonBuscarEnLogActionPerformed

    /**
     * El botón de buscar también se acciona con la tecla enter, en el campo de
     * texto.
     */
    private void textoBusquedaLogKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textoBusquedaLogKeyPressed
        if (evt.getExtendedKeyCode() == VK_ENTER)
        botonBuscarEnLog.doClick();
    }//GEN-LAST:event_textoBusquedaLogKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog VentanaMasInfo;
    private javax.swing.JButton botonAbrirDirectorio;
    private javax.swing.JButton botonAcceso;
    private javax.swing.JButton botonAceptarMas;
    private javax.swing.JButton botonAnalizar;
    private javax.swing.JButton botonBuscar;
    private javax.swing.JButton botonBuscarEnLog;
    private javax.swing.JButton botonDescarga;
    private javax.swing.JButton botonSeleccionarFichero;
    private javax.swing.JButton botonSiguiente;
    private javax.swing.JButton botonVer;
    private javax.swing.JButton botonVerDatosArranque;
    private javax.swing.ButtonGroup botonesPais;
    private javax.swing.JButton cerrarDialogo;
    private javax.swing.JDialog dialogoAviso;
    private javax.swing.JFileChooser elegirDirectorio;
    private datechooser.beans.DateChooserCombo elegirFechaLog;
    private javax.swing.JLabel estadoAbrir;
    private javax.swing.JLabel estadoEmpleado;
    private javax.swing.JLabel estadoMaestras;
    private javax.swing.JFileChooser exploradorTerminal;
    private javax.swing.JComboBox<String> filtro;
    private javax.swing.JComboBox<String> funcionElegida;
    private javax.swing.JLabel infoAvisoLabel;
    private java.awt.Label infoConexion;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JDialog inicioSesionTerminal;
    private javax.swing.JTextField ipText;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labelInfoUser;
    private javax.swing.JLabel labelLogoUser;
    private javax.swing.JPasswordField passwordText;
    private javax.swing.JComboBox<String> seleccionaBox;
    private javax.swing.JTable tablaDatos;
    private javax.swing.JTable tablaInfo;
    private javax.swing.JTextField textoBusqueda;
    private javax.swing.JTextField textoBusquedaLog;
    private javax.swing.JTextField userText;
    private javax.swing.JDialog ventanaAnalisis;
    private javax.swing.JDialog ventanaDatos;
    private javax.swing.JDialog ventanaExplorador;
    private javax.swing.JDialog ventanaExploradorTerminal;
    private javax.swing.JDialog ventanaVerMas;
    private javax.swing.JDialog ventanaVersiones;
    private javax.swing.JLabel versionApp;
    private javax.swing.JLabel versionCf;
    private javax.swing.JLabel versionData;
    private javax.swing.JLabel versionDevice;
    private javax.swing.JLabel versionFw;
    // End of variables declaration//GEN-END:variables
}
