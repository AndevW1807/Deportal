export function fieldMessage(field: {
  touched: () => boolean;
  errors: () => readonly { message?: string }[];
}): string | null {
  if (!field.touched()) {
    return null;
  }

  return field.errors()[0]?.message ?? null;
}
