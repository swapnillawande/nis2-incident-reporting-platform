interface PaginationControlsProps {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isLoading?: boolean;
  onPageChange: (page: number) => void;
  onSizeChange: (size: number) => void;
}

const PAGE_SIZE_OPTIONS = [10, 20, 50];

function PaginationControls({
  page,
  size,
  totalElements,
  totalPages,
  isLoading = false,
  onPageChange,
  onSizeChange,
}: PaginationControlsProps) {
  const safeTotalPages = Math.max(totalPages, 1);
  const startItem = totalElements === 0 ? 0 : page * size + 1;
  const endItem = Math.min((page + 1) * size, totalElements);

  return (
    <div className="pagination-bar">
      <div className="pagination-summary">
        Showing <strong>{startItem}</strong>-<strong>{endItem}</strong> of{" "}
        <strong>{totalElements}</strong>
      </div>

      <div className="pagination-actions">
        <label className="pagination-size">
          Rows
          <select
            value={size}
            onChange={(event) => onSizeChange(Number(event.target.value))}
            disabled={isLoading}
          >
            {PAGE_SIZE_OPTIONS.map((pageSize) => (
              <option key={pageSize} value={pageSize}>
                {pageSize}
              </option>
            ))}
          </select>
        </label>

        <button
          className="btn-secondary compact"
          onClick={() => onPageChange(page - 1)}
          disabled={isLoading || page <= 0}
        >
          Prev
        </button>

        <span className="pagination-page">
          Page {page + 1} of {safeTotalPages}
        </span>

        <button
          className="btn-secondary compact"
          onClick={() => onPageChange(page + 1)}
          disabled={isLoading || page >= safeTotalPages - 1}
        >
          Next
        </button>
      </div>
    </div>
  );
}

export default PaginationControls;
