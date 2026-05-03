package com.cg.frontend.controller;

import com.cg.frontend.dto.DiscountDto;
import com.cg.frontend.dto.PageMetaDto;
import com.cg.frontend.dto.SaleDto;
import com.cg.frontend.dto.StoreDto;
import com.cg.frontend.service.StoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // ── GET /stores  — list with pagination + search ──────────────────────────

    @GetMapping
    public String listStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String search,
            Model model) {

        List<StoreDto> stores;
        PageMetaDto pageMeta;

        if (search != null && !search.isBlank()) {
            stores = storeService.searchStores(search);
            pageMeta = new PageMetaDto();
            pageMeta.setTotalElements(stores.size());
            pageMeta.setTotalPages(1);
            pageMeta.setNumber(0);
            pageMeta.setSize(stores.size());
        } else {
            stores = storeService.getAllStores(page, size);
            pageMeta = storeService.getPageMeta(page, size);
        }

        model.addAttribute("stores", stores);
        model.addAttribute("pageMeta", pageMeta);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        model.addAttribute("pageTitle", "Stores — Amritansu's Module");
        return "stores/list";
    }

    // ── GET /stores/{storId}  — detail with sales + discounts ─────────────────

    @GetMapping("/{storId}")
    public String viewStore(@PathVariable String storId, Model model) {
        StoreDto store = storeService.getStoreById(storId);
        if (store == null) {
            return "redirect:/stores";
        }

        List<SaleDto>     sales     = storeService.getSalesByStore(storId);
        List<DiscountDto> discounts = storeService.getDiscountsByStore(storId);

        model.addAttribute("store",     store);
        model.addAttribute("sales",     sales);
        model.addAttribute("discounts", discounts);
        model.addAttribute("pageTitle", "Store Detail — " + store.getStorName());
        return "stores/detail";
    }

    // ── POST /stores/create ───────────────────────────────────────────────────

    @PostMapping("/create")
    public String createStore(@ModelAttribute StoreDto store, RedirectAttributes ra) {
        String error = storeService.createStore(store);
        if (error != null) {
            ra.addFlashAttribute("errorMsg", error);
            return "redirect:/stores?error=create";
        }
        return "redirect:/stores?success=create";
    }

    // ── POST /stores/update/{storId} ──────────────────────────────────────────

    @PostMapping("/update/{storId}")
    public String updateStore(@PathVariable String storId,
                              @ModelAttribute StoreDto store,
                              RedirectAttributes ra) {
        store.setStorId(storId);
        String error = storeService.updateStore(storId, store);
        if (error != null) {
            ra.addFlashAttribute("errorMsg", error);
            return "redirect:/stores/" + storId + "?error=update";
        }
        return "redirect:/stores/" + storId + "?success=update";
    }
}
