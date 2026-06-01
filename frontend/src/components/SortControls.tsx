import type { SortDirection } from "../types/pagination";

interface SortOption {
  label: string;
  value: string;
}

interface SortControlsProps {
  options: SortOption[];
  sortBy: string;
  sortDir: SortDirection;
  onSortByChange: (sortBy: string) => void;
  onSortDirChange: (sortDir: SortDirection) => void;
}

function SortControls({
  options,
  sortBy,
  sortDir,
  onSortByChange,
  onSortDirChange,
}: SortControlsProps) {
  return (
    <div className="sort-controls">
      <div className="filter-group">
        <label>Sort By</label>
        <select
          value={sortBy}
          onChange={(event) => onSortByChange(event.target.value)}
        >
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      <button
        className="btn-secondary sort-direction-button"
        onClick={() => onSortDirChange(sortDir === "asc" ? "desc" : "asc")}
        type="button"
      >
        {sortDir === "asc" ? "Asc" : "Desc"}
      </button>
    </div>
  );
}

export default SortControls;
