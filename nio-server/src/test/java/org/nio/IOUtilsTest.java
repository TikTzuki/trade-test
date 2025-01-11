package org.nio;

import com.nio.wallet.grpc.WalletServiceOuterClass.TransferRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import org.nio.util.IOUtils;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IOUtilsTest {
    @Test
    void getSizeReturnsCorrectSizeForValidInput() {
        ByteBuf input = Unpooled.buffer();
        input.writeByte(0xcf);
        input.writeByte(0x02);
        input.writeByte(0x0a);
        input.writeByte(0x24);

        int rs = IOUtils.getSize(input);

        assertThat(rs).isEqualTo(335);
    }

    @Test
    void testDeserialize() throws IOException {
        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();

        TransferRequest request1 = TransferRequest.newBuilder()
                .setUserId("userId")
                .build();
        request1.writeDelimitedTo(outputStream1);
        TransferRequest request2 = TransferRequest.newBuilder()
                .setUserId("userId2")
                .setAmount("8".repeat(300))
                .build();
        request2.writeDelimitedTo(outputStream2);

        ByteBuf byteBuf = Unpooled.wrappedBuffer(
                outputStream1.toByteArray(), outputStream2.toByteArray()
        );
        ByteBufFlux flux = ByteBufFlux.fromInbound(Flux.just(byteBuf));

        List it = IOUtils.deserializeMessages(flux, TransferRequest.newBuilder())
                .collectList()
                .block();
        assertThat(it.get(0))
                .isEqualTo(request1);
        assertThat(it.get(1))
                .isEqualTo(request1);
        System.out.println(it);
    }

    @Test
    void getSizeThrowsExceptionForInvalidInput() {
        ByteBuf input = Unpooled.buffer();
        input.writeInt(128);
        assertThrows(RuntimeException.class, () -> IOUtils.getSize(input));
    }
}
