package com.java.cp04.Amazin.enumerator;

public enum StatusEmail {
    ENVIADO,    // entregue ao servidor SMTP
    SIMULADO,   // SMTP desabilitado (app.mail.enabled=false): apenas registrado no banco
    FALHA       // erro ao enviar
}
