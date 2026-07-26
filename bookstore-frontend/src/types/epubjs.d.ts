declare module "epubjs" {
    export interface Rendition {
        display(target?: string): Promise<void>;
        prev(): void;
        next(): void;
        on(event: string, callback: (location: unknown) => void): void;
        destroy(): void;
    }

    export interface Book {
        renderTo(element: HTMLElement, options: { width: string | number; height: string | number }): Rendition;
        ready: Promise<void>;
        destroy(): void;
    }

    export default function ePub(url: string): Book;
}