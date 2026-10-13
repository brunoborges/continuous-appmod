package com.example.todo;

import java.io.DataInputStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaVersionTest {

    @Test
    void compilesApplicationForJava17() throws Exception {
        try (DataInputStream bytecode = new DataInputStream(
                TodoApplication.class.getResourceAsStream("TodoApplication.class"))) {
            assertThat(bytecode.readInt()).isEqualTo(0xCAFEBABE);
            bytecode.readUnsignedShort();
            assertThat(bytecode.readUnsignedShort()).isEqualTo(61);
        }
    }
}
