/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package csplugins.id.mapping.ui;

import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

import org.bridgedb.DataSource;



// support different editors for each row in a column
/**
 * Table for selecting which attribute to use for matching nodes
 *
 *
 */
public class SourceAttributeSelectionTable extends JTable{
    private IDTypeSelectionTableModel model;

    private Set<DataSourceWrapper> supportedIDType;

    private CheckComboBoxSelectionChangedListener idTypeSelectionChangedListener;

    private List<JComboBox> attributeComboBoxes;
    private List<String> selectedAttribute;

    private List<CheckComboBox> typeComboBoxes;
    private List<JButton> rmvBtns;
    private JButton addBtn;

    private java.awt.Color defBgColor = (new JScrollPane()).getBackground();

    private int rowCount;

    private final String colHeaderAtt = "Key Attribute";
    private final String colHeaderSrc = "Source ID Type(s)";
    private final String colHeaderBtn = " ";

    private Vector<String> attributes;

    public SourceAttributeSelectionTable() {
        super();
        initializeAttibutes();

        supportedIDType = new TreeSet();
        attributeComboBoxes = new Vector();
        selectedAttribute = new Vector();
        typeComboBoxes = new Vector();
        rmvBtns = new Vector();
        addBtn = new JButton("Insert");
        addBtn.setBackground(defBgColor);
        
        this.setGridColor(defBgColor);

        rowCount = 0;

        model = new IDTypeSelectionTableModel();
        setModel(model);

        setRowHeight(25);

        TableCellRenderer renderer = getTableHeader().getDefaultRenderer();
        JLabel label = (JLabel)renderer;
        label.setHorizontalAlignment(JLabel.CENTER);

        final SourceAttributeSelectionTable this_table = this;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = this_table.getColumnModel();
                int column = columnModel.getColumnIndexAtX(e.getX());
                int row    = e.getY() / this_table.getRowHeight();

                if(row >= this_table.getRowCount() || row < 0 ||
                   column >= this_table.getColumnCount() || column < 0)
                  return;

                String colName = this_table.getColumnName(column);
                if (colName.compareTo(colHeaderBtn)==0) {
                    if (row<rowCount) {
                        this_table.removeRow(row);
                    } else if (row==rowCount) {
                        this_table.addRow();
                    }
                }
            }
        });

        setPreferredColumnWidths(new double[]{0.5,0.4,0.1});

        setColumnEditorAndCellRenderer();

    }

    private void initializeAttibutes() {
        attributes = new Vector<String>();
        //TODO remove in Cytoscape3
        attributes.add("ID");
        //TODO: modify if local attribute implemented

        List<String> list = Arrays.asList(cytoscape.Cytoscape.getNodeAttributes().getAttributeNames());
        Collections.sort(list);

        attributes.addAll(list);
    }

    void setIDTypeSelectionChangedListener(
            CheckComboBoxSelectionChangedListener idTypeSelectionChangedListener) {
        this.idTypeSelectionChangedListener = idTypeSelectionChangedListener;
    }

    public void setSupportedIDType(final Set<DataSource> types) {
        if (types==null) {
            throw new NullPointerException();
        }

        Map<String,Set<DataSource>> oldMap = this.getSourceAttrType();
        
        supportedIDType = new TreeSet();
        for (DataSource type : types) {
            supportedIDType.add(DataSourceWrapper.getInstance(type));
        }

        //select the id type previously selected
        this.setSourceAttrType(oldMap);
        
        model.fireTableStructureChanged();
        setColumnEditorAndCellRenderer();
    }

    public void setSourceAttrType(Map<String,Set<DataSource>> srcAttrType) {
        if (srcAttrType==null) return;

        this.clearRows();

        for (Map.Entry<String,Set<DataSource>> entry : srcAttrType.entrySet()) {
            String attr = entry.getKey();
            Set<DataSource> dss = entry.getValue();
            this.addRow(attr, dss);
        }

        if(this.rowCount==0) {
            addRow();
        }


    }

    public Map<String,Set<DataSource>> getSourceAttrType() {
        Map<String,Set<DataSource>> ret = new LinkedHashMap();
//        if (supportedIDType.isEmpty()) {
//            return ret;
//        }
        
        for (int i=0; i<rowCount; i++) {
            String attr = (String)selectedAttribute.get(i);
            //TODO REMOVE IN CY3
//            if (attr.compareTo("ID")==0) {
//                attr = cytoscape.data.Semantics.CANONICAL_NAME;
//            }

            Set<DataSource> types = null;
            if (!supportedIDType.isEmpty()) {
            Object[] dsws = typeComboBoxes.get(i).getSelectedItems();
            if (dsws!=null) {
                    types = new HashSet();
                for (Object dsw : dsws) {
                    types.add(((DataSourceWrapper)dsw).DataSource());
                }
            }
            }
            ret.put(attr, types);
        }

        return ret;
    }

    public Set<DataSource> getSelectedIDTypes() {
        Set<DataSource> ret = new HashSet();

        if (supportedIDType.isEmpty()) {
            return ret;
        }

        for (int i=0; i<rowCount; i++) {
            Object[] dsws = typeComboBoxes.get(i).getSelectedItems();
            if (dsws!=null) {
                for (Object dsw : dsws) {
                    ret.add(((DataSourceWrapper)dsw).DataSource());
                }
            }
        }

        return ret;
    }

    protected void setColumnEditorAndCellRenderer() {
        TableColumnModel colModel = getColumnModel();

        // attribute column
        int iCol = colModel.getColumnIndex(colHeaderAtt);
        TableColumn column = getColumnModel().getColumn(iCol);
        RowTableCellEditor rowEditor = new RowTableCellEditor(this);

        for (int ir=0; ir<rowCount; ir++) {
            rowEditor.setEditorAt(ir, new  DefaultCellEditor(attributeComboBoxes.get(ir)));
        }
        column.setCellEditor(rowEditor);
        column.setCellRenderer(new ComboBoxTableCellRenderer());

        // type column
        iCol = colModel.getColumnIndex(colHeaderSrc);
        column = getColumnModel().getColumn(iCol);
        rowEditor = new RowTableCellEditor(this);

        for (int ir=0; ir<rowCount; ir++) {
            rowEditor.setEditorAt(ir, new  DefaultCellEditor(typeComboBoxes.get(ir)));
        }
        column.setCellEditor(rowEditor);

        if (supportedIDType.isEmpty()) {
            column.setCellRenderer(new TableCellRenderer() {
                private DefaultTableCellRenderer  defaultRenderer = new DefaultTableCellRenderer();

                //@Override
                public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (row<rowCount) {
                       if (isSelected) {
                               label.setBackground(table.getSelectionBackground());
                               label.setForeground(table.getSelectionForeground());
                       } else {
                               label.setBackground(table.getBackground());
                               label.setForeground(table.getForeground());
                       }
                       label.setText("No supported ID type");
                       label.setToolTipText("Please select a non-empty ID mapping source first.");
                       return label;
                    } else if (row==rowCount) {
                        label.setBackground(defBgColor);
                        label.setForeground(defBgColor);
                        label.setText("");
                        return label;
                    } else {
                        return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    }
                }
            });
        } else {
            column.setCellRenderer(new ComboBoxTableCellRenderer());
        }

        iCol = colModel.getColumnIndex(colHeaderBtn);
        column = getColumnModel().getColumn(iCol);
        column.setCellRenderer(new ButtonRenderer());
    }

    public void addRow() {
        addRow(null, null);
    }

    private void addRow(String attr, Set<DataSource> dss) {
        if (rowCount>=attributes.size()) {
            JOptionPane.showMessageDialog(cytoscape.Cytoscape.getDesktop(),
                    "All attributes have been used. Cannot add more.");
            return;
        }
        
        final JComboBox cb = new JComboBox(attributes);
        cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //TODO: check if the attribute has already been used
                String selected = (String)cb.getSelectedItem();
                int i = 0;
                int n = attributeComboBoxes.size();
                while (i<n && attributeComboBoxes.get(i)!=cb)
                    i++;
                if (i==n) // should not happen
                    return;
                if (selected.compareTo(selectedAttribute.get(i))==0)
                    return;
                if (selectedAttribute.contains(selected)) {
                    JOptionPane.showMessageDialog(cytoscape.Cytoscape.getDesktop(),
                            "This attribute has already been selected.\n" +
                            "Please selecte another one.");
                    cb.setSelectedItem(selectedAttribute.get(i));
                } else {
                    selectedAttribute.set(i, selected);
                }
            }
        });

        if (attr!=null || !attributes.contains(attr)) {
            for (String at : attributes) {
                if (!selectedAttribute.contains(at)) {
                    cb.setSelectedItem(at);
                    break;
                }
            }
        } else {
            cb.setSelectedItem(attr);
        }
        selectedAttribute.add((String)cb.getSelectedItem());
        attributeComboBoxes.add(cb);

        Set<DataSourceWrapper> selectedDsws = new HashSet();
        if (dss!=null) {
                for (DataSource ds : dss) {
                    DataSourceWrapper dsw = DataSourceWrapper.getInstance(ds);
                    if (supportedIDType.contains(dsw))
                        selectedDsws.add(dsw);
                }
        }
        CheckComboBox cc = new CheckComboBox(supportedIDType, selectedDsws);
        cc.addSelectionChangedListener(idTypeSelectionChangedListener);
        typeComboBoxes.add(cc);

        JButton button = new JButton("Remove");
        rmvBtns.add(button);

        rowCount++;

        setColumnEditorAndCellRenderer();
        fireTableDataChanged();
    }

    public void clearRows() {
        int[] rows = new int[rowCount];
        for (int i=0; i<rowCount; i++) {
            rows[i] = i;
        }

        removeRows(rows);
    }

    public void removeRows(final int[] rows) {
        TreeSet<Integer> setrows = new TreeSet();
        for (int row : rows) {
            setrows.add(row);
        }

        Vector<Integer> vecrows = new Vector(setrows);
        int nsel = vecrows.size();
        for (int i=nsel-1; i>=0; i--) {
            int row = vecrows.get(i);
            if (row<0 || row>rowCount) {
                throw new java.lang.IndexOutOfBoundsException();
            }

            attributeComboBoxes.remove(row);
            selectedAttribute.remove(row);
            typeComboBoxes.remove(row);
            rmvBtns.remove(row);
        }

        rowCount -= nsel;

        setColumnEditorAndCellRenderer();
        fireTableDataChanged();
    }

    public void removeRow(final int row) {
        int[] rows = new int[1];
        rows[0] = row;

        removeRows(rows);
    }

    void fireTableDataChanged() {
            model.fireTableDataChanged();
    }

    private void setPreferredColumnWidths(double[] percentages) {
        java.awt.Dimension tableDim = this.getPreferredSize();

        double total = 0; 
        for(int i = 0; i < getColumnModel().getColumnCount(); i++)
            total += percentages[i];

        for(int i = 0; i < getColumnModel().getColumnCount(); i++)
        {
            TableColumn column = getColumnModel().getColumn(i);
            column.setPreferredWidth((int) (tableDim.width * (percentages[i] / total)));
        }
   }

    private class IDTypeSelectionTableModel extends AbstractTableModel {
        private final String[] columnNames = {colHeaderSrc, colHeaderAtt, colHeaderBtn};

        //@Override
        public int getColumnCount() {
            return 3; // select; network; attribute; id types
        }

        //@Override
        public int getRowCount() {
            return rowCount+1;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        //@Override
        public Object getValueAt(int row, int col) {
            String colName = getColumnName(col);
            if (row<rowCount) {
                if (colName.compareTo(colHeaderAtt)==0) {
                    return attributeComboBoxes.get(row);
                }
                if (colName.compareTo(colHeaderSrc)==0) {
                    return typeComboBoxes.get(row);
                }
                if (colName.compareTo(colHeaderBtn)==0) {
                    return rmvBtns.get(row);
                }

                throw new java.lang.IndexOutOfBoundsException();
            } else if (row==rowCount) {
                if (colName.compareTo(colHeaderBtn)==0) {
                    return addBtn;
                }

                return null;
            } else {
                throw new java.lang.IndexOutOfBoundsException();
            }
        }

        @Override
        public Class getColumnClass(int c) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            String colName = getColumnName(col);
            if (row<rowCount) {
                if (colName.compareTo(colHeaderAtt)==0) {
                    return true;
                }
                if (colName.compareTo(colHeaderSrc)==0) {
                    return !supportedIDType.isEmpty();
                }
                if (colName.compareTo(colHeaderBtn)==0) {
                    return false;
                }

                throw new java.lang.IndexOutOfBoundsException();
            } else if (row==rowCount) {
                return false;
            } else {
                throw new java.lang.IndexOutOfBoundsException();
            }
        }

    }

    // render checkcombobox
    private class ComboBoxTableCellRenderer implements TableCellRenderer {
        private DefaultTableCellRenderer defaultRenderer;

        public ComboBoxTableCellRenderer() {
            defaultRenderer = new DefaultTableCellRenderer();
        }

        //@Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
            if (row==rowCount) {
                JLabel label = (JLabel) (new DefaultTableCellRenderer()).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(defBgColor);
                label.setForeground(defBgColor);
                return label;
            } else if (row<rowCount) {
                if (!(value instanceof JComboBox)) {
                    return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                } else {
                    return (JComboBox)value;
                }
            } else {
                return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
    }

    private class ButtonRenderer implements TableCellRenderer {
        private DefaultTableCellRenderer defaultRenderer;

        public ButtonRenderer() {
            defaultRenderer = new DefaultTableCellRenderer();
        }

      public Component getTableCellRendererComponent(JTable table, Object value,
                       boolean isSelected, boolean hasFocus, int row, int column) {
          if (!(value instanceof JButton)) {
              return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          }

          return (JButton)value;
      }
    }

}


