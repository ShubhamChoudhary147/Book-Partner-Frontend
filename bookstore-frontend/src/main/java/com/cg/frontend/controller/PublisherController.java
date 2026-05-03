package com.cg.frontend.controller;

import com.cg.frontend.dto.PageMetaDto;
import com.cg.frontend.dto.PublisherDto;
import com.cg.frontend.dto. PublisherTitleDto;
import com.cg.frontend.service.PublisherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    // ── Page 2 — Publisher List ───────────────────────────────────────────────

    @GetMapping
    public String listPublishers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String search,
            Model model) {

        List<PublisherDto> publishers;
        PageMetaDto pageMeta;

        if (search != null && !search.isBlank()) {
            publishers = publisherService.searchPublishers(search);
            pageMeta = new PageMetaDto();
            pageMeta.setTotalElements(publishers.size());
            pageMeta.setTotalPages(1);
            pageMeta.setNumber(0);
            pageMeta.setSize(publishers.size());
        } else {
            publishers = publisherService.getAllPublishers(page, size);
            pageMeta  = publisherService.getPageMeta(page, size);
        }

        model.addAttribute("publishers",    publishers);
        model.addAttribute("pageMeta",      pageMeta);
        model.addAttribute("currentPage",   page);
        model.addAttribute("pageSize",      size);
        model.addAttribute("search",        search);
        model.addAttribute("newPublisher",  new PublisherDto());
        model.addAttribute("pageTitle",     "Publishers — Kartik's Module");
        return "publishers/list";
    }

    // ── Page 3 — Publisher Detail ─────────────────────────────────────────────

    @GetMapping("/{pubId}")
    public String viewPublisher(@PathVariable String pubId, Model model) {
        PublisherDto publisher = publisherService.getPublisherById(pubId);
        if (publisher == null) {
            return "redirect:/publishers";
        }
        List<PublisherTitleDto> titles = publisherService.getTitlesByPublisher(pubId);
        model.addAttribute("publisher",  publisher);
        model.addAttribute("titles",     titles);
        model.addAttribute("pageTitle",  "Publisher Detail — " + publisher.getDisplayName());
        return "publishers/detail";
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @PostMapping("/create")
    public String createPublisher(@ModelAttribute PublisherDto publisher, RedirectAttributes ra) {
        String error = publisherService.createPublisher(publisher);
        if (error != null) {
            ra.addFlashAttribute("errorMsg", error);
            return "redirect:/publishers?error=create";
        }
        return "redirect:/publishers?success=create";
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @PostMapping("/update/{pubId}")
    public String updatePublisher(@PathVariable String pubId,
                                  @ModelAttribute PublisherDto publisher,
                                  RedirectAttributes ra) {
        publisher.setPubId(pubId);
        String error = publisherService.updatePublisher(pubId, publisher);
        if (error != null) {
            ra.addFlashAttribute("errorMsg", error);
            return "redirect:/publishers?error=update";
        }
        return "redirect:/publishers?success=update";
    }
}
