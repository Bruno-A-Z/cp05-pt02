package com.java.cp04.Amazin.service;

import com.java.cp04.Amazin.dto.CadastroForm;
import com.java.cp04.Amazin.enumerator.Role;
import com.java.cp04.Amazin.model.Usuario;
import com.java.cp04.Amazin.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean usernameExiste(String username) {
        return usuarioRepository.existsByUsername(username.trim());
    }

    public boolean emailExiste(String email) {
        return usuarioRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Transactional
    public Usuario cadastrar(CadastroForm form) {
        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().trim());
        usuario.setUsername(form.getUsername().trim());
        usuario.setEmail(form.getEmail().trim().toLowerCase());
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        usuario.setRole(Role.USER); // cadastro público sempre cria perfil USER
        return usuarioRepository.save(usuario);
    }
}
