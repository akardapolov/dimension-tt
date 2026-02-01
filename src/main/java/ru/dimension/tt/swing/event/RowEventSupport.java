package ru.dimension.tt.swing.event;

import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import ru.dimension.tt.model.TTTableModel;

/**
 * Installs row event listeners on a JTable.
 */
public final class RowEventSupport {
  private RowEventSupport() {}

  public static <T> void install(JTable table, TTTableModel<T> model, RowActions<T> actions) {
    if (actions == null || actions.isEmpty()) return;

    // Mouse events: click, double-click, right-click, context menu
    if (actions.onClick() != null || actions.onDoubleClick() != null
        || actions.onRightClick() != null || actions.contextMenuProvider() != null) {
      installMouseListener(table, model, actions);
    }

    // Hover events
    if (actions.onHover() != null) {
      installHoverListener(table, model, actions.onHover());
    }

    // Selection change events
    if (actions.onSelectionChanged() != null) {
      installSelectionListener(table, model, actions.onSelectionChanged());
    }

    // Enter key
    if (actions.onRowEnterKey() != null) {
      installEnterKeyListener(table, model, actions.onRowEnterKey());
    }
  }

  private static <T> void installMouseListener(JTable table, TTTableModel<T> model, RowActions<T> actions) {
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        RowEvent<T> event = resolveRowEvent(table, model, e);
        if (event == null) return;

        if (SwingUtilities.isLeftMouseButton(e)) {
          if (e.getClickCount() == 2 && actions.onDoubleClick() != null) {
            actions.onDoubleClick().accept(event);
          } else if (e.getClickCount() == 1 && actions.onClick() != null) {
            actions.onClick().accept(event);
          }
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        handlePopup(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        handlePopup(e);
      }

      private void handlePopup(MouseEvent e) {
        if (!e.isPopupTrigger()) return;

        RowEvent<T> event = resolveRowEvent(table, model, e);
        if (event == null) return;

        // Select the row on right-click (common UX)
        table.setRowSelectionInterval(event.viewRowIndex(), event.viewRowIndex());

        if (actions.onRightClick() != null) {
          actions.onRightClick().accept(event);
        }

        if (actions.contextMenuProvider() != null) {
          JPopupMenu menu = actions.contextMenuProvider().apply(event);
          if (menu != null) {
            menu.show(table, e.getX(), e.getY());
          }
        }
      }
    });
  }

  private static <T> void installHoverListener(JTable table, TTTableModel<T> model,
                                               java.util.function.BiConsumer<T, Boolean> handler) {
    final int[] lastHoveredRow = {-1};

    table.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int viewRow = table.rowAtPoint(e.getPoint());
        int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;

        if (modelRow != lastHoveredRow[0]) {
          // Exit previous
          if (lastHoveredRow[0] >= 0 && lastHoveredRow[0] < model.getRowCount()) {
            T prevRow = model.itemAt(lastHoveredRow[0]);
            handler.accept(prevRow, false);
          }
          // Enter new
          if (modelRow >= 0 && modelRow < model.getRowCount()) {
            T newRow = model.itemAt(modelRow);
            handler.accept(newRow, true);
          }
          lastHoveredRow[0] = modelRow;
        }
      }
    });

    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        if (lastHoveredRow[0] >= 0 && lastHoveredRow[0] < model.getRowCount()) {
          T prevRow = model.itemAt(lastHoveredRow[0]);
          handler.accept(prevRow, false);
        }
        lastHoveredRow[0] = -1;
      }
    });
  }

  private static <T> void installSelectionListener(JTable table, TTTableModel<T> model,
                                                   java.util.function.Consumer<RowSelectionEvent<T>> handler) {
    final int[] lastSelectedModelRow = {-1};

    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;

        int viewRow = table.getSelectedRow();
        int modelRow = viewRow >= 0 ? table.convertRowIndexToModel(viewRow) : -1;

        if (modelRow == lastSelectedModelRow[0]) return;

        T prevRow = null;
        int prevModelRow = lastSelectedModelRow[0];
        if (prevModelRow >= 0 && prevModelRow < model.getRowCount()) {
          prevRow = model.itemAt(prevModelRow);
        }

        T newRow = null;
        if (modelRow >= 0 && modelRow < model.getRowCount()) {
          newRow = model.itemAt(modelRow);
        }

        lastSelectedModelRow[0] = modelRow;

        RowSelectionEvent<T> event = new RowSelectionEvent<>(newRow, prevRow, modelRow, prevModelRow);
        handler.accept(event);
      }
    });
  }

  private static <T> void installEnterKeyListener(JTable table, TTTableModel<T> model,
                                                  java.util.function.Consumer<T> handler) {
    // Register Enter key action
    InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap am = table.getActionMap();

    String actionKey = "tt.rowEnterAction";
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), actionKey);
    am.put(actionKey, new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= model.getRowCount()) return;
        T row = model.itemAt(modelRow);
        handler.accept(row);
      }
    });
  }

  private static <T> RowEvent<T> resolveRowEvent(JTable table, TTTableModel<T> model, MouseEvent e) {
    Point p = e.getPoint();
    int viewRow = table.rowAtPoint(p);
    if (viewRow < 0) return null;

    int modelRow = table.convertRowIndexToModel(viewRow);
    if (modelRow < 0 || modelRow >= model.getRowCount()) return null;

    T row = model.itemAt(modelRow);
    return RowEvent.of(row, modelRow, viewRow, e);
  }
}