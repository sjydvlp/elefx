package com.sjydvlp.elefx.component.table;

/** The row and value of an overflowing cell. */
public record EleFXTableTooltipData<S, T> (S row, EleFXTableColumn<S, T> column, T cellValue) {
}
