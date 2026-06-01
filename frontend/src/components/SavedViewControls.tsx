import { useState } from "react";
import type { SavedViewResponse } from "../types/savedView";

interface SavedViewControlsProps {
  savedViews: SavedViewResponse[];
  saveName: string;
  isSaving?: boolean;
  placeholder?: string;
  onApply: (savedView: SavedViewResponse) => void;
  onDelete: (savedView: SavedViewResponse) => void;
  onNameChange: (name: string) => void;
  onSave: () => void;
}

function SavedViewControls({
  savedViews,
  saveName,
  isSaving = false,
  placeholder = "My saved view",
  onApply,
  onDelete,
  onNameChange,
  onSave,
}: SavedViewControlsProps) {
  const [selectedSavedViewId, setSelectedSavedViewId] = useState("");

  const handleSavedViewChange = (savedViewId: string) => {
    setSelectedSavedViewId(savedViewId);
    const savedView = savedViews.find((item) => String(item.id) === savedViewId);

    if (savedView) {
      onApply(savedView);
    }
  };

  return (
    <section className="saved-view-bar">
      <div className="saved-view-picker">
        <div className="filter-group">
          <label>Saved Views</label>
          <select
            value={selectedSavedViewId}
            onChange={(event) => handleSavedViewChange(event.target.value)}
            disabled={savedViews.length === 0}
          >
            <option value="">
              {savedViews.length === 0 ? "No saved views" : "Apply saved view"}
            </option>
            {savedViews.length > 0 &&
              savedViews.map((savedView) => (
                <option key={savedView.id} value={savedView.id}>
                  {savedView.name}
                </option>
              ))}
          </select>
        </div>

        <button
          className="btn-secondary"
          disabled={savedViews.length === 0}
          onClick={() => {
            const savedView = savedViews.find(
              (item) => String(item.id) === selectedSavedViewId
            );
            if (savedView) {
              onDelete(savedView);
              setSelectedSavedViewId("");
            }
          }}
          type="button"
        >
          Delete
        </button>
      </div>

      <div className="saved-view-save">
        <div className="filter-group">
          <label>Save Current View</label>
          <input
            value={saveName}
            onChange={(event) => onNameChange(event.target.value)}
            placeholder={placeholder}
          />
        </div>
        <button
          className="btn-secondary"
          disabled={isSaving || !saveName.trim()}
          onClick={onSave}
          type="button"
        >
          {isSaving ? "Saving..." : "Save View"}
        </button>
      </div>
    </section>
  );
}

export default SavedViewControls;
