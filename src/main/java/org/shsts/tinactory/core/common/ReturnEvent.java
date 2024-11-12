package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ReturnEvent<A, R> extends Event<A> {
    private final R defaultRet;

    public ReturnEvent(R defaultRet) {
        this.defaultRet = defaultRet;
    }

    public Token<R> newToken() {
        return new Token<>(defaultRet);
    }

    public R getDefaultReturn() {
        return defaultRet;
    }

    @SuppressWarnings("unchecked")
    public void invoke(Handler<?, ?> handler, A arg, Token<R> token) {
        ((Handler<A, R>) handler).handle(arg, token);
    }

    @FunctionalInterface
    public interface Handler<A, R> {
        void handle(A arg, Token<R> token);
    }

    public static class Token<R> {
        private R ret;

        private Token(R ret) {
            this.ret = ret;
        }

        public R getReturn() {
            return ret;
        }

        public void setReturn(R ret) {
            this.ret = ret;
        }
    }
}
