package cz.oz.mavenhoe;

/**
 *
 * @author Ondrej Zizka
 */
public abstract class Thief<T> {

    private T loot;
    public T steal( T val ){ this.conceal(val); return val; }
    public abstract void conceal( T val );
    public T sell( ){ return this.loot; }

}
