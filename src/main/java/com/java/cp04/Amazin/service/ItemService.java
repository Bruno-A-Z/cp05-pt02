package com.java.cp04.Amazin.service;

import com.java.cp04.Amazin.model.Item;
import com.java.cp04.Amazin.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<Item> listarTodos() {
        return itemRepository.findAll();
    }

    public Item buscarPorId(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Item não encontrado com id: " + id));
    }

    public Item criar(Item item) {
        return itemRepository.save(item);
    }

    public Item atualizar(Long id, Item itemAtualizado) {
        Item item = buscarPorId(id);
        item.setName(itemAtualizado.getName());
        item.setKind(itemAtualizado.getKind());
        item.setSector(itemAtualizado.getSector());
        item.setSize(itemAtualizado.getSize());
        item.setPrice(itemAtualizado.getPrice());
        return itemRepository.save(item);
    }

    public Item atualizarParcial(Long id, Item itemParcial) {
        Item item = buscarPorId(id);
        if (itemParcial.getName() != null) item.setName(itemParcial.getName());
        if (itemParcial.getKind() != null) item.setKind(itemParcial.getKind());
        if (itemParcial.getSector() != null) item.setSector(itemParcial.getSector());
        if (itemParcial.getSize() != null) item.setSize(itemParcial.getSize());
        if (itemParcial.getPrice() != null) item.setPrice(itemParcial.getPrice());
        return itemRepository.save(item);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        itemRepository.deleteById(id);
    }

}