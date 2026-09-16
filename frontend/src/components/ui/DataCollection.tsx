import type { ReactNode } from "react";

export interface DataColumn<T> {
  key: string;
  header: string;
  render: (item: T) => ReactNode;
  align?: "left" | "right";
}

interface DataCollectionProps<T> {
  items: T[];
  columns: DataColumn<T>[];
  getKey: (item: T) => string;
  renderMobile: (item: T) => ReactNode;
  caption: string;
}

export function DataCollection<T>({
  items,
  columns,
  getKey,
  renderMobile,
  caption,
}: DataCollectionProps<T>) {
  return (
    <>
      <div className="table-wrap">
        <table className="data-table">
          <caption className="sr-only">{caption}</caption>
          <thead>
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  scope="col"
                  className={column.align === "right" ? "align-right" : undefined}
                >
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={getKey(item)}>
                {columns.map((column) => (
                  <td
                    key={column.key}
                    className={column.align === "right" ? "align-right" : undefined}
                  >
                    {column.render(item)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="mobile-records">
        {items.map((item) => (
          <article className="mobile-record" key={getKey(item)}>
            {renderMobile(item)}
          </article>
        ))}
      </div>
    </>
  );
}
