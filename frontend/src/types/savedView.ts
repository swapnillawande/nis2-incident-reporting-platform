export type SavedViewType = "INCIDENTS" | "USERS" | "AUDIT_LOGS";

export interface SavedViewResponse {
  id: number;
  viewType: SavedViewType;
  name: string;
  filterJson: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSavedViewRequest {
  viewType: SavedViewType;
  name: string;
  filterJson: string;
}
