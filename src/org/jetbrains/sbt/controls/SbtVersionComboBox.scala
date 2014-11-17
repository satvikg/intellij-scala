package org.jetbrains.sbt.controls

import java.awt._
import java.awt.event.ActionListener
import javax.swing._

/**
 * @author Nikolay Obedin
 * @since 11/17/14.
 */
class SbtVersionComboBox extends JComboBox {
  private val placeholder = "Autodetect"
  private val versions = Seq(
    "0.12.4", "0.13.0", "0.13.1", "0.13.2", "0.13.3", "0.13.4", "0.13.5", "0.13.6"
  )

  setModel(new DefaultComboBoxModel(versions.toArray[Object]))
  setEditable(true)
  setSelectedItem("")
  setPreferredSize(new Dimension(120, getPreferredSize.height))

  setEditor(new ComboBoxEditor(){
    val field = new JTextField() {
      override def paintComponent(g: Graphics) = {
        super.paintComponent(g)
        if (getText.isEmpty) {
          val g2d = g.asInstanceOf[Graphics2D]
          g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
          g2d.setColor(getDisabledTextColor)
          g2d.drawString(placeholder, getInsets.left, g.getFontMetrics.getMaxAscent + getInsets.top)
        }
      }
    }

    def getEditorComponent: Component =
      field

    def setItem(obj: Object) =
      if (obj == null) {
        field.setText("")
      } else {
        field.setText(obj.toString)
      }

    def getItem: Object = {
      val str = field.getText
      if (str.isEmpty)
        null
      else
        str
    }

    def selectAll() =
      field.selectAll()

    def addActionListener(l: ActionListener) = {}
    def removeActionListener(l: ActionListener) = {}
  })
}

