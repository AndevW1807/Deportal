export function fieldMessage(field: {
  touched: () => boolean;
  errors: () => readonly { message?: string }[];
}): string | null {
  if (!field.touched()) {
    return null;
  }

  return field.errors()[0]?.message ?? null;
}

export function fieldMessages(
  fields: readonly { errors: () => readonly { message?: string }[] }[],
): string | null {
  const messages = fields.flatMap((field) =>
    field.errors().flatMap((error) => (error.message ? [error.message] : [])),
  );

  return messages.length === 0 ? null : messages.join(' ');
}
