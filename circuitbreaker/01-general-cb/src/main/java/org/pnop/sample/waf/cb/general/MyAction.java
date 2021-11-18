package org.pnop.sample.waf.cb.general;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyAction implements Action<Boolean> {

    private static Logger logger = LoggerFactory.getLogger(MyAction.class);

    @Override
    public void run(Boolean throwException) throws Exception {
        if (throwException) {
            logger.info("呼び出し失敗");
            throw new IOException("I/O エラーが発生しました");
        }
        logger.info("呼び出し成功");
        return;
    }
}
