/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package analizalog;

/**
 *
 * @author 6002310
 */
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Esta clase establece el color de las filas de la tabla de trazas.
 */
public class RenderCeldaDefault extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value.toString().contains("NOK")) {
            this.setOpaque(true);
            this.setForeground(Color.BLACK);
        } else {
            this.setForeground(Color.BLACK);
        }

        return this;
    }
}
