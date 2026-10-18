package com.java.cp04.Amazin.controller;

import com.java.cp04.Amazin.assembler.ItemModelAssembler;
import com.java.cp04.Amazin.model.Item;
import com.java.cp04.Amazin.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
@Tag(name = "Item", description = "CRUD de itens do mercado express")
public class ItemController {

    private final ItemService itemService;
    private final ItemModelAssembler assembler;

    @GetMapping
    @Operation(summary = "Listar todos os itens")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public CollectionModel<EntityModel<Item>> listarTodos() {
        List<EntityModel<Item>> itens = itemService.listarTodos().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(itens,
                linkTo(methodOn(ItemController.class).listarTodos()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar item por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public EntityModel<Item> buscarPorId(@PathVariable Long id) {
        Item item = itemService.buscarPorId(id);
        return assembler.toModel(item);
    }

    @PostMapping
    @Operation(summary = "Criar novo item")
    @ApiResponse(responseCode = "201", description = "Item criado com sucesso")
    public ResponseEntity<EntityModel<Item>> criar(@RequestBody Item item) {
        Item novoItem = itemService.criar(item);
        EntityModel<Item> entityModel = assembler.toModel(novoItem);

        return ResponseEntity
                .created(linkTo(methodOn(ItemController.class).buscarPorId(novoItem.getId())).toUri())
                .body(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar item por completo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public ResponseEntity<EntityModel<Item>> atualizar(@PathVariable Long id, @RequestBody Item item) {
        Item itemAtualizado = itemService.atualizar(id, item);
        return ResponseEntity.ok(assembler.toModel(itemAtualizado));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar item parcialmente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado parcialmente"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public ResponseEntity<EntityModel<Item>> atualizarParcial(@PathVariable Long id, @RequestBody Item item) {
        Item itemAtualizado = itemService.atualizarParcial(id, item);
        return ResponseEntity.ok(assembler.toModel(itemAtualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar item por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item deletado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        itemService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}