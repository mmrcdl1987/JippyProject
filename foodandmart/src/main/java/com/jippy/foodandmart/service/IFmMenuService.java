package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmMenuCopyRequestDTO;
import com.jippy.foodandmart.dto.FmMenuCopyResultDTO;
import com.jippy.foodandmart.dto.FmMenuItemDTO;
import com.jippy.foodandmart.dto.FmOutletSummaryDTO;

import java.util.List;

public interface IFmMenuService {

    List<FmOutletSummaryDTO> listAllOutlets();

    List<FmMenuItemDTO> getMenuByOutlet(Integer outletId);

    FmMenuCopyResultDTO copyMenu(FmMenuCopyRequestDTO req);
}
