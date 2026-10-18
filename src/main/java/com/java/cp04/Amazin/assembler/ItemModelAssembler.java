package com.java.cp04.Amazin.assembler;

import com.java.cp04.Amazin.controller.ItemController;
import com.java.cp04.Amazin.model.Item;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ItemModelAssembler implements RepresentationModelAssembler<Item, EntityModel<Item>> {

    @Override
    public EntityModel<Item> toModel(Item item) {
        return EntityModel.of(item,
                linkTo(methodOn(ItemController.class).buscarPorId(item.getId())).withSelfRel(),
                linkTo(methodOn(ItemController.class).listarTodos()).withRel("itens"),
                linkTo(methodOn(ItemController.class).deletar(item.getId())).withRel("deletar"));
    }

}