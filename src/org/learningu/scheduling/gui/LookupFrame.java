package org.learningu.scheduling.gui;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.learningu.scheduling.graph.Program;
import org.learningu.scheduling.graph.ProgramObject;

public final class LookupFrame extends JFrame {

  static final int RETURN_CAP = 10;

  private final Program program;

  private final JComboBox<ProgramObjectType> typeSelector;
  private final JTextField queryField;
  private Map<Integer, ProgramObject<?>> mapping = ImmutableMap.of();
  private final JComboBox<ProgramObject<?>> resultSelector;
  private final JLabel resultLabel;

  @Inject
  LookupFrame(Program program) {
    super("Lookup Application");
    this.program = program;
    JPanel overall = new JPanel();
    overall.setLayout(new GridLayout(1, 2));
    JPanel queryPanel = new JPanel();
    queryPanel.setLayout(new GridLayout(3, 1));
    typeSelector = new JComboBox<ProgramObjectType>(ProgramObjectType.values());
    queryField = new JTextField();
    queryPanel.add(typeSelector);
    queryPanel.add(queryField);
    JButton searchButton = new JButton("Search");
    queryPanel.add(searchButton);

    JPanel resultPanel = new JPanel();
    resultPanel.setLayout(new BorderLayout());
    resultSelector = new JComboBox<ProgramObject<?>>();
    resultSelector.setRenderer(new BasicComboBoxRenderer() {

      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value == null) {
          setText("");
        } else {
          setText(((ProgramObject<?>) value).getShortDescription());
        }
        return this;
      }
    });
    resultSelector.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (resultSelector.getSelectedIndex() == -1) {
          resultLabel.setText("");
        } else {
          setResultFor(resultSelector.getItemAt(resultSelector.getSelectedIndex()));
        }
      }
    });

    resultPanel.add(resultSelector, BorderLayout.NORTH);
    resultLabel = new JLabel();
    resultPanel.add(resultLabel, BorderLayout.CENTER);
    overall.add(queryPanel);
    overall.add(resultPanel);
    searchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        search();
      }
    });
    setContentPane(overall);
    setSize(500, 500);
  }

  private void setResultFor(@Nullable ProgramObject<?> obj) {
    if (obj == null) {
      resultLabel.setText("");
    } else {
      resultLabel.setText(String.format("<html>ID: %d<br>%s</html>", obj.getId(), obj.toString()));
    }
  }

  private void search() {
    int index = typeSelector.getSelectedIndex();
    if (index != -1) {
      ProgramObjectType type = typeSelector.getItemAt(index);
      String query = queryField.getText();
      if (!query.isEmpty()) {
        List<? extends ProgramObject<?>> objects = type.retrieveMatching(program, query);
        resultSelector.removeAllItems();
        for (ProgramObject<?> object : objects) {
          resultSelector.addItem(object);
        }
        if (!objects.isEmpty()) {
          resultSelector.setSelectedIndex(0);
          setResultFor(resultSelector.getItemAt(0));
        }
        repaint();
        return;
        
      }
    }
    resultSelector.removeAllItems();
    resultLabel.setText("");
  }
}
