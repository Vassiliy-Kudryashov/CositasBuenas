package cositas.buenas.getlog;

import java.util.Date;

class Revision implements Comparable<Revision> {
    final Author author;
    final Date date;

    public Revision(Author author, Date date) {
        this.author = author;
        this.date = date;
    }

    @Override
    public int compareTo(Revision o) {
        int i = author.compareTo(o.author);
        if (i != 0) {
            return i;
        }
        return date.compareTo(o.date);
    }

    @Override
    public String toString() {
        return author + " " + Getlog.SIMPLE_FORMAT.format(date);
    }
}
