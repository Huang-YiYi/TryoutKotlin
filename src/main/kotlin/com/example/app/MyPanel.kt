package com.example.app

import de.siegmar.fastcsv.reader.CsvReader
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.nio.charset.StandardCharsets
import javax.swing.*

//class Currentlineclass {
//    companion object {
//        var currentLine = 0
//
//    }
//
//}

    class MyPanel : JPanel() {
        private val jcomp1: JButton
        private val jcomp2: JButton
        private val jcomp3: JButton
        private val jcomp4: JLabel
        private val jcomp5: JTextArea
        private val jcomp6: JLabel

        private var SCRIPT_PATH = ""

        companion object {
            fun open() {
                val frame = JFrame("THE NECESSARY STAGE APPLICATION")
                frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                frame.contentPane.add(MyPanel())
                frame.pack()
                frame.isVisible = true
            }
        }

        init {
            //construct components
            jcomp1 = JButton("Previous subtitles")
            jcomp2 = JButton("Next Subtitles")
            jcomp3 = JButton("Upload files")
            jcomp4 = JLabel("WELCOME TO THE NECESSARY STAGE")
            jcomp5 = JTextArea(5, 5)
            jcomp6 =
                JLabel(ImageIcon("/Users/eleven/IdeaProjects/closed-caption-service/src/main/kotlin/com/example/app/smart2.png"))
            jcomp1.addActionListener(Button1Click())
            jcomp2.addActionListener(Button2Click())
            jcomp3.addActionListener(Button3Click())

            //adjust size and set layout
            preferredSize = Dimension(686, 504)
            layout = null

            //add components
            add(jcomp1)
            add(jcomp2)
            add(jcomp3)
            add(jcomp4)
            add(jcomp5)
            add(jcomp6)

            //set component bounds (only needed by Absolute Positioning)
            jcomp1.setBounds(185, 410, 160, 40)
            jcomp2.setBounds(355, 410, 160, 40)
            jcomp3.setBounds(185, 455, 330, 35)
            jcomp4.setBounds(240, 30, 242, 30)
            jcomp5.setBounds(185, 185, 330, 210)
            jcomp6.setBounds(260, 20, 200, 200)

        }

        internal class Button3Click : ActionListener {
            override fun actionPerformed(e: ActionEvent) {

                val fileChooser = JFileChooser()
                fileChooser.dialogTitle = "Specify a file to save"

                val userSelection = fileChooser.showOpenDialog(MyPanel())

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    val fileToSave = fileChooser.selectedFile
                    SCRIPT_PATH = fileToSave.absolutePath
                    println("Save as file: " + fileToSave.absolutePath)
                }

            }
        }

//        val lines: List<String> by lazy {
//            val file = File(SCRIPT_PATH)
//            val csvReader = CsvReader()
//
//            val csv = csvReader.read(file, StandardCharsets.UTF_8)
//            csv.rows.map {
//                it.getField(0)
//            }
//        }

//        val service = ClosedCaptionService(SERVICE_PORT, object : ClosedCaptionService.ClientRequestListener {
//            override fun onRequestScript(): List<String> {
//                return lines
//            }
//
//            override fun onRequestCurrentLineNumber(): Int {
//                return Currentlineclass.currentLine
//            }
//        })



        internal class Button1Click : ActionListener {

            override fun actionPerformed(e: ActionEvent) {

            }
        }

        internal class Button2Click : ActionListener {
            override fun actionPerformed(e: ActionEvent) {

            }
        }


//        fun open() {
//            val frame = JFrame("THE NECESSARY STAGE APPLICATION")
//            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
//            frame.contentPane.add(MyPanel2())
//            frame.pack()
//            frame.isVisible = true
//        }


    }



