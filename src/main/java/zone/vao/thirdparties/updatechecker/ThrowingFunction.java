package zone.vao.thirdparties.updatechecker;

@FunctionalInterface
interface ThrowingFunction<T,R,E extends Exception> {
    R apply(T t) throws E;
}
