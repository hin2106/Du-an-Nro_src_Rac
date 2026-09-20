
package managers;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsManager<E> implements IManager<E> {

    protected List<E> list = new ArrayList<>();

    public void add(E e) {
        list.add(e);
    }

    public void remove(E e) {
        list.remove(e);
    }

    public abstract E findByID(int id);

    public List<E> getList() {
        return list;
    }
}