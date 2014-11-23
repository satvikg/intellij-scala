package org.jetbrains.sbt.controls

import java.awt._
import java.awt.event.{FocusEvent, FocusListener}
import java.beans.{PropertyChangeEvent, PropertyChangeListener}
import javax.swing._
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.plaf.basic.{BasicComboBoxUI, ComboPopup}

import com.intellij.openapi.ui.{ComboBox, FixedComboBoxEditor}
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.JBTextField
import com.intellij.util.ReflectionUtil
import com.intellij.util.ui.UIUtil
import org.jetbrains.sbt.project.settings.SbtSettings

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
  setPreferredSize(new Dimension(120, getPreferredSize.height))

  setEditor(new FixedComboBoxEditor(){
    val field =
      if (SystemInfo.isMac && UIUtil.isUnderAquaLookAndFeel)
        new MacComboBoxTextField
      else
        new TextFieldWithPlaceholder

    override def getEditorComponent: Component =
      field

    override def setItem(obj: Object) =
      if (obj == null) {
        field.setText("")
      } else {
        field.setText(obj.toString)
      }

    override def getItem: Object = {
      val str = field.getText
      if (str.isEmpty)
        null
      else
        str
    }

    override def selectAll() =
      field.selectAll()

    class TextFieldWithPlaceholder extends JBTextField {
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

    // Mostly copy-pasted from com.intellij.openapi.ui.FixedComboBoxEditor.MacComboBoxTextField
    class MacComboBoxTextField extends TextFieldWithPlaceholder with DocumentListener with FocusListener {
      import com.intellij.openapi.ui.FixedComboBoxEditor._

      setBorder(if (isEnabled) EDITOR_BORDER else DISABLED_EDITOR_BORDER)

      val inputMap = getInputMap

      inputMap.put(KeyStroke.getKeyStroke("DOWN"), "aquaSelectNext")
      inputMap.put(KeyStroke.getKeyStroke("KP_DOWN"), "aquaSelectNext")
      inputMap.put(KeyStroke.getKeyStroke("UP"), "aquaSelectPrevious")
      inputMap.put(KeyStroke.getKeyStroke("KP_UP"), "aquaSelectPrevious")

      inputMap.put(KeyStroke.getKeyStroke("HOME"), "aquaSelectHome")
      inputMap.put(KeyStroke.getKeyStroke("END"), "aquaSelectEnd")
      inputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), "aquaSelectPageUp")
      inputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "aquaSelectPageDown")

      inputMap.put(KeyStroke.getKeyStroke("ENTER"), "aquaEnterPressed")
      inputMap.put(KeyStroke.getKeyStroke("SPACE"), "aquaSpacePressed")


      addPropertyChangeListener(new PropertyChangeListener() {
        override def propertyChange(evt: PropertyChangeEvent): Unit =  {
          if ("enabled".equals(evt.getPropertyName)) {
            setBorder(if (evt.getNewValue == true) EDITOR_BORDER else DISABLED_EDITOR_BORDER)
            repaint()
          }
        }
      })

      addFocusListener(this)


      override def hasFocus: Boolean = getParent match {
        case parent : ComboBox => parent.hasFocus
        case _ => super.hasFocus
      }

      override def focusGained(e: FocusEvent) =
        repaintCombobox()

      override def focusLost(e: FocusEvent) =
        repaintCombobox()

      def repaintCombobox() = getParent match {
        case parent : JComponent if parent.getClientProperty("JComboBox.isTableCellEditor") == false =>
          Option(parent.getParent) foreach (_.repaint)
        case _ => // do nothing
      }

      override def getMinimumSize = {
        val minimumSize = super.getMinimumSize
        new Dimension(minimumSize.width, minimumSize.height + 2)
      }

      override def getPreferredSize =
        getMinimumSize

      override def setBounds(x: Int, y: Int, width: Int, height: Int) =
        UIUtil.setComboBoxEditorBounds(x, y, width, height, this)

      override def insertUpdate(e: DocumentEvent) =
        textChanged()

      override def removeUpdate(e: DocumentEvent) =
        textChanged()

      override def changedUpdate(e: DocumentEvent) =
        textChanged()


      def getComboboxPopup(comboBox: JComboBox): ComboPopup = comboBox.getUI match {
        case ui: BasicComboBoxUI =>
          ReflectionUtil.getField(classOf[BasicComboBoxUI], ui, classOf[ComboPopup], "popup")
        case _ => null
      }

      def textChanged() {
        val ancestor = SwingUtilities.getAncestorOfClass(classOf[JComboBox], this)

        if (ancestor == null || !ancestor.isVisible)
          return

        ancestor match {
          case comboBox : JComboBox if comboBox.isPopupVisible =>
            val popup = getComboboxPopup(comboBox)
            val s = field.getText
            val listModel = comboBox.getModel
            if (s.nonEmpty) {
              for (i <- 0 to listModel.getSize) {
                Option(listModel.getElementAt(i)).map(_.toString).foreach { str =>
                  if (str.startsWith(s) || str == s) {
                    popup.getList.setSelectedIndex(i)
                    return
                  }
                }
              }

            }
            popup.getList.clearSelection()
        }
      }
    }
  })
}


