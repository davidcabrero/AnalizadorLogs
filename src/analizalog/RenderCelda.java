/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package analizalog;

/**
 *
 * @author 6002310
 */
import java.awt.Component;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Esta clase pinta de rojo las fases que no han tenido un resultado correcto. En la opción fases.
 */
public class RenderCelda extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value.toString().contains("NOK")) {
            this.setOpaque(true);
            this.setForeground(Color.RED);
        } else {
            this.setForeground(Color.BLACK);
        }

        return this;
    }
}
