package gui;




import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import javax.swing.text.Document;

public class JTextFieldX extends JTextField {

  private String placeHolder;
  private boolean placeHolderShown;

  public JTextFieldX() {
    this(null, null, 0);
  }

  public JTextFieldX(String text) {
    this(null, text, 0);
  }

  public JTextFieldX(int columns) {
    this(null, null, columns);
  }

  public JTextFieldX(String text, int columns) {
    this(null, text, columns);
  }

  public JTextFieldX(Document doc, String text, int columns) {
    super(doc, text, columns);
    initPlaceHolder();
  }

  private void initPlaceHolder() {
    placeHolder = "";
    placeHolderShown = false;

    addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        hidePlaceHolder();
      }

      @Override
      public void focusLost(FocusEvent e) {
        showPlaceHolder();
      }
    });
  }

  public String getPlaceHolder() {
    return placeHolder;
  }

  public void setPlaceHolder(String placeHolder) {
    this.placeHolder = placeHolder;
    showPlaceHolder();
  }

  private void showPlaceHolder() {
    if (getText().equals("")) {
      setForeground(Color.gray);
      setText(placeHolder);
      placeHolderShown = true;
    }
  }

  public void hidePlaceHolder() {
    if (placeHolderShown) {
      setForeground(Color.black);
      setText("");
      placeHolderShown = false;
    }
  }

  @Override
  public String getText() {
    if (placeHolderShown) {
      return "";
    } else {
      return super.getText();
    }
  }

}
