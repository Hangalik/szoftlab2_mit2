package org.bme.mit.iir;

public class IndexMain {
    public static final void main(String args[]) {
        if (args.length != 1) {
            System.exit(-1);
            return;
        }

        IndexMaker maker = new IndexMaker("index.txt");
        maker.processDirectory(args[0]);
        maker.makeFile();
    }
}
