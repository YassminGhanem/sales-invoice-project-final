package com.sales.controller;

import com.sales.model.Invoice;
import com.sales.model.InvoicesTableModel;
import com.sales.model.Line;
import com.sales.model.LinesTableModel;
import com.sales.view.InvoiceDialog;
import com.sales.view.InvoiceFrame;
import com.sales.view.LineDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Controller implements ActionListener, ListSelectionListener {

    private InvoiceFrame _invoiceFrame;
    private InvoiceDialog invoiceDialog;
    private LineDialog lineDialog;

    public Controller(InvoiceFrame invoiceFrame) {
        this._invoiceFrame = invoiceFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        System.out.println("Action: " + actionCommand);
        switch (actionCommand) {
            case "Load File":
                GettingFile();
                break;
            case "Save File":
                InsertFile();
                break;
            case "Create New Invoice":
                CreateInvoice();
                break;
            case "Delete Invoice":
                RemoveInvoice();
                break;
            case "Create New Item":
                InsertItem();
                break;
            case "Delete Item":
                RemoveItem();
                break;
            case "createInvoiceCancel":
                CancelInvoice();
                break;
            case "createInvoiceOK":
                CompleteInvoice();
                break;
            case "createLineOK":
                CreateLine();
                break;
            case "createLineCancel":
                CancelLine();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedIndex = _invoiceFrame.getInvoiceTable().getSelectedRow();
        if (selectedIndex != -1) {
            System.out.println("You have selected row: " + selectedIndex);
            Invoice currentInvoice = _invoiceFrame.getInvoices().get(selectedIndex);
            _invoiceFrame.getInvoiceNumLabel().setText("" + currentInvoice.getNum());
            _invoiceFrame.getInvoiceDateLabel().setText(currentInvoice.getDate());
            _invoiceFrame.getCustomerNameLabel().setText(currentInvoice.getCustomer());
            _invoiceFrame.getInvoiceTotalLabel().setText("" + currentInvoice.getInvoiceTotal());
            LinesTableModel linesTableModel = new LinesTableModel(currentInvoice.getLines());
            _invoiceFrame.getLineTable().setModel(linesTableModel);
            linesTableModel.fireTableDataChanged();
        }
    }

    private void GettingFile() {
        JFileChooser fc = new JFileChooser();
        try {
            int result = fc.showOpenDialog(_invoiceFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fc.getSelectedFile();
                Path headerPath = Paths.get(headerFile.getAbsolutePath());
                List<String> headerLines = Files.readAllLines(headerPath);
                System.out.println("Invoices have been read");
                // 1,22-11-2020,Ali
                // 2,13-10-2021,Saleh
                // 3,09-01-2019,Ibrahim
                ArrayList<Invoice> invoicesArray = new ArrayList<>();
                for (String headerLine : headerLines) {
                    String[] headerParts = headerLine.split(",");
                    int invoiceNum = Integer.parseInt(headerParts[0]);
                    String invoiceDate = headerParts[1];
                    String customerName = headerParts[2];

                    Invoice invoice = new Invoice(invoiceNum, invoiceDate, customerName);
                    invoicesArray.add(invoice);
                }
                System.out.println("Check point");
                result = fc.showOpenDialog(_invoiceFrame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fc.getSelectedFile();
                    Path linePath = Paths.get(lineFile.getAbsolutePath());
                    List<String> lineLines = Files.readAllLines(linePath);
                    System.out.println("Lines have been read");
                    for (String lineLine : lineLines) {
                        String lineParts[] = lineLine.split(",");
                        int invoiceNum = Integer.parseInt(lineParts[0]);
                        String itemName = lineParts[1];
                        double itemPrice = Double.parseDouble(lineParts[2]);
                        int count = Integer.parseInt(lineParts[3]);
                        Invoice inv = null;
                        for (Invoice invoice : invoicesArray) {
                            if (invoice.getNum() == invoiceNum) {
                                inv = invoice;
                                break;
                            }
                        }

                        Line line = new Line(itemName, itemPrice, count, inv);
                        inv.getLines().add(line);
                    }
                    System.out.println("Check point");
                }
                _invoiceFrame.setInvoices(invoicesArray);
                InvoicesTableModel invoicesTableModel = new InvoicesTableModel(invoicesArray);
                _invoiceFrame.setInvoicesTableModel(invoicesTableModel);
                _invoiceFrame.getInvoiceTable().setModel(invoicesTableModel);
                _invoiceFrame.getInvoicesTableModel().fireTableDataChanged();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void InsertFile() {
        ArrayList<Invoice> invoices = _invoiceFrame.getInvoices();
        String headers = "";
        String lines = "";
        for (Invoice invoice : invoices) {
            String invCSV = invoice.getAsCSV();
            headers += invCSV;
            headers += "\n";

            for (Line line : invoice.getLines()) {
                String lineCSV = line.getAsCSV();
                lines += lineCSV;
                lines += "\n";
            }
        }
        System.out.println("Check point");
        try {
            JFileChooser fc = new JFileChooser();
            int result = fc.showSaveDialog(_invoiceFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fc.getSelectedFile();
                FileWriter hfw = new FileWriter(headerFile);
                hfw.write(headers);
                hfw.flush();
                hfw.close();
                result = fc.showSaveDialog(_invoiceFrame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fc.getSelectedFile();
                    FileWriter lfw = new FileWriter(lineFile);
                    lfw.write(lines);
                    lfw.flush();
                    lfw.close();
                }
            }
        } catch (Exception ex) {

        }
    }

    private void CreateInvoice() {
        invoiceDialog = new InvoiceDialog(_invoiceFrame);
        invoiceDialog.setVisible(true);
    }

    private void RemoveInvoice() {
        int selectedRow = _invoiceFrame.getInvoiceTable().getSelectedRow();
        if (selectedRow != -1) {
            _invoiceFrame.getInvoices().remove(selectedRow);
            _invoiceFrame.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void InsertItem() {
        lineDialog = new LineDialog(_invoiceFrame);
        lineDialog.setVisible(true);
    }

    private void RemoveItem() {
        int selectedRow = _invoiceFrame.getLineTable().getSelectedRow();

        if (selectedRow != -1) {
            LinesTableModel linesTableModel = (LinesTableModel) _invoiceFrame.getLineTable().getModel();
            linesTableModel.getLines().remove(selectedRow);
            linesTableModel.fireTableDataChanged();
            _invoiceFrame.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void CancelInvoice() {
        invoiceDialog.setVisible(false);
        invoiceDialog.dispose();
        invoiceDialog = null;
    }

    private void CompleteInvoice() {
        String date = invoiceDialog.getInvDateField().getText();
        String customer = invoiceDialog.getCustNameField().getText();
        int num = _invoiceFrame.getNextInvoiceNum();

        Invoice invoice = new Invoice(num, date, customer);
        _invoiceFrame.getInvoices().add(invoice);
        _invoiceFrame.getInvoicesTableModel().fireTableDataChanged();
        invoiceDialog.setVisible(false);
        invoiceDialog.dispose();
        invoiceDialog = null;
    }

    private void CreateLine() {
        String item = lineDialog.getItemNameField().getText();
        String countStr = lineDialog.getItemCountField().getText();
        String priceStr = lineDialog.getItemPriceField().getText();
        int count = Integer.parseInt(countStr);
        double price = Double.parseDouble(priceStr);
        int selectedInvoice = _invoiceFrame.getInvoiceTable().getSelectedRow();
        if (selectedInvoice != -1) {
            Invoice invoice = _invoiceFrame.getInvoices().get(selectedInvoice);
            Line line = new Line(item, price, count, invoice);
            invoice.getLines().add(line);
            LinesTableModel linesTableModel = (LinesTableModel) _invoiceFrame.getLineTable().getModel();
            //linesTableModel.getLines().add(line);
            linesTableModel.fireTableDataChanged();
            _invoiceFrame.getInvoicesTableModel().fireTableDataChanged();
        }
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

    private void CancelLine() {
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

}
