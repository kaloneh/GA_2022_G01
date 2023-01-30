export interface IGameObject {
  id?: number;
  x?: number | null;
  y?: number | null;
  bitmapContentType?: string | null;
  bitmap?: string | null;
  isEnabled?: boolean | null;
}

export const defaultValue: Readonly<IGameObject> = {
  isEnabled: false,
};
