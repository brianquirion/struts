package org.apache.struts2.jasper.compiler;

import org.apache.struts2.jasper.JasperException;

import java.util.HashMap;

final class SmapGenVisitor extends Node.Visitor {

    private SmapStratum smap;
    private boolean breakAtLF;
    private HashMap innerClassMap;

    SmapGenVisitor(SmapStratum s, boolean breakAtLF, HashMap map) {
        this.smap = s;
        this.breakAtLF = breakAtLF;
        this.innerClassMap = map;
    }

    public void visitBody(Node n) throws JasperException {
        SmapStratum smapSave = smap;
        String innerClass = n.getInnerClassName();
        if (innerClass != null) {
            this.smap = (SmapStratum) innerClassMap.get(innerClass);
        }
        super.visitBody(n);
        smap = smapSave;
    }

    public void visit(Node.Declaration n) throws JasperException {
        doSmapText(n);
    }

    public void visit(Node.Expression n) throws JasperException {
        doSmapText(n);
    }

    public void visit(Node.Scriptlet n) throws JasperException {
        doSmapText(n);
    }

    public void visit(Node.IncludeAction n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.ForwardAction n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.GetProperty n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.SetProperty n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.UseBean n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.PlugIn n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.CustomTag n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.UninterpretedTag n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.JspElement n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.JspText n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.NamedAttribute n) throws JasperException {
        visitBody(n);
    }

    public void visit(Node.JspBody n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.InvokeAction n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.DoBodyAction n) throws JasperException {
        doSmap(n);
        visitBody(n);
    }

    public void visit(Node.ELExpression n) throws JasperException {
        doSmap(n);
    }

    public void visit(Node.TemplateText n) throws JasperException {
        Mark mark = n.getStart();
        if (mark == null) {
            return;
        }

        //Add the file information
        String fileName = mark.getFile();
        smap.addFile(SmapUtil.unqualify(fileName), fileName);

        //Add a LineInfo that corresponds to the beginning of this node
        int iInputStartLine = mark.getLineNumber();
        int iOutputStartLine = n.getBeginJavaLine();
        int iOutputLineIncrement = breakAtLF? 1: 0;
        smap.addLineData(iInputStartLine, fileName, 1, iOutputStartLine,
                         iOutputLineIncrement);

        // Output additional mappings in the text
        java.util.ArrayList extraSmap = n.getExtraSmap();

        if (extraSmap != null) {
            for (int i = 0; i < extraSmap.size(); i++) {
                iOutputStartLine += iOutputLineIncrement;
                smap.addLineData(
                    iInputStartLine+((Integer)extraSmap.get(i)).intValue(),
                    fileName,
                    1,
                    iOutputStartLine,
                    iOutputLineIncrement);
            }
        }
    }

    private void doSmap(
        Node n,
        int inLineCount,
        int outIncrement,
        int skippedLines) {
        Mark mark = n.getStart();
        if (mark == null) {
            return;
        }

        String unqualifiedName = SmapUtil.unqualify(mark.getFile());
        smap.addFile(unqualifiedName, mark.getFile());
        smap.addLineData(
            mark.getLineNumber() + skippedLines,
            mark.getFile(),
            inLineCount - skippedLines,
            n.getBeginJavaLine() + skippedLines,
            outIncrement);
    }

    private void doSmap(Node n) {
        doSmap(n, 1, n.getEndJavaLine() - n.getBeginJavaLine(), 0);
    }

    private void doSmapText(Node n) {
        String text = n.getText();
        int index = 0;
        int next = 0;
        int lineCount = 1;
        int skippedLines = 0;
        boolean slashStarSeen = false;
        boolean beginning = true;

        // Count lines inside text, but skipping comment lines at the
        // beginning of the text.
        while ((next = text.indexOf('\n', index)) > -1) {
            if (beginning) {
                String line = text.substring(index, next).trim();
                if (!slashStarSeen && line.startsWith("/*")) {
                    slashStarSeen = true;
                }
                if (slashStarSeen) {
                    skippedLines++;
                    int endIndex = line.indexOf("*/");
                    if (endIndex >= 0) {
                        // End of /* */ comment
                        slashStarSeen = false;
                        if (endIndex < line.length() - 2) {
                            // Some executable code after comment
                            skippedLines--;
                            beginning = false;
                        }
                    }
                } else if (line.length() == 0 || line.startsWith("//")) {
                    skippedLines++;
                } else {
                    beginning = false;
                }
            }
            lineCount++;
            index = next + 1;
        }

        doSmap(n, lineCount, 1, skippedLines);
    }
}