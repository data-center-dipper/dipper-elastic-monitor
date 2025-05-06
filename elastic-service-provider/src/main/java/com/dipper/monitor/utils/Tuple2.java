package com.dipper.monitor.utils;

import java.util.Objects;

public class Tuple2<K, V> {
    protected K k;
    protected V v;

    public K getK() {
        return this.k;
    }

    public void setK(K k) {
        this.k = k;
    }

    public V getV() {
        return this.v;
    }

    public void setV(V v) {
        this.v = v;
    }

    public Tuple2(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public static <K, V> Tuple2<K, V> of(K k, V v) {
        return new Tuple2<>(k, v);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return (Objects.equals(this.k, tuple2.k) &&
                Objects.equals(this.v, tuple2.v));
    }

    public int hashCode() {
        return Objects.hash(this.k, this.v);
    }

    public String toString() {
        return "Tuple2{k=" + this.k + ", v=" + this.v + "}";
    }
}